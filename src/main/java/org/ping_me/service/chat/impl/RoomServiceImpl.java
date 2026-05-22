package org.ping_me.service.chat.impl;

import lombok.RequiredArgsConstructor;
import org.ping_me.config.s3.S3Service;
import org.ping_me.dto.request.chat.room.AddGroupMembersRequest;
import org.ping_me.dto.request.chat.room.CreateGroupRoomRequest;
import org.ping_me.dto.request.chat.room.CreateOrGetDirectRoomRequest;
import org.ping_me.dto.request.chat.room.JoinGroupByLinkRequest;
import org.ping_me.dto.request.chat.room.LeaveGroupRequest;
import org.ping_me.dto.request.chat.room.ReviewGroupJoinRequest;
import org.ping_me.dto.request.chat.room.UpdateGroupSettingsRequest;
import org.ping_me.dto.response.chat.room.GroupJoinRequestResponse;
import org.ping_me.dto.response.chat.room.GroupSettingsResponse;
import org.ping_me.dto.response.chat.room.JoinGroupByLinkResponse;
import org.ping_me.dto.response.chat.room.RoomResponse;
import org.ping_me.model.User;
import org.ping_me.model.chat.GroupJoinRequest;
import org.ping_me.model.chat.GroupSettings;
import org.ping_me.model.chat.Room;
import org.ping_me.model.chat.RoomParticipant;
import org.ping_me.model.common.RoomMemberId;
import org.ping_me.model.constant.GroupJoinRequestStatus;
import org.ping_me.model.constant.RoomRole;
import org.ping_me.model.constant.RoomType;
import org.ping_me.repository.jpa.auth.UserRepository;
import org.ping_me.repository.jpa.chat.DeletedMessageRepository;
import org.ping_me.repository.jpa.chat.GroupJoinRequestRepository;
import org.ping_me.repository.jpa.chat.GroupSettingsRepository;
import org.ping_me.repository.jpa.chat.RoomParticipantRepository;
import org.ping_me.repository.jpa.chat.RoomRepository;
import org.ping_me.service.chat.MessageService;
import org.ping_me.service.chat.RoomService;
import org.ping_me.service.chat.event.room.*;
import org.ping_me.service.user.CurrentUserProvider;
import org.ping_me.utils.mapper.ChatMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin 8/25/2025
 *
 **/
@Service
@Transactional
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    // SERVICE
    private final MessageService messageService;
    private final S3Service s3Service;

    // PROVIDER
    private final CurrentUserProvider currentUserProvider;

    // REPOSITORY
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final DeletedMessageRepository deletedMessageRepository;
    private final GroupJoinRequestRepository groupJoinRequestRepository;
    private final GroupSettingsRepository groupSettingsRepository;
    private final UserRepository userRepository;

    // PUBLISHER
    private final ApplicationEventPublisher eventPublisher;

    // UTILS
    private final ChatMapper chatMapper;

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024L;

    /* ========================================================================== */
    /*                         TẠO HOẶC TÌM PHÒNG CHAT 1-1                        */
    /* ========================================================================== */
    @Override
    public RoomResponse createOrGetDirectRoom(CreateOrGetDirectRoomRequest createOrGetDirectRoomRequest) {
        var currentUser = currentUserProvider.get();

        if (currentUser.getId().equals(createOrGetDirectRoomRequest.getTargetUserId()))
            throw new IllegalArgumentException("Bạn không thể tự nhắn tin cho chính mình");

        if (!userRepository.existsById(createOrGetDirectRoomRequest.getTargetUserId()))
            throw new IllegalArgumentException("Người dùng cần nhắn tin không tồn tại");

        String directKey = buildDirectKey(currentUser.getId(), createOrGetDirectRoomRequest.getTargetUserId());

        var room = roomRepository.findByDirectKey(directKey).orElse(null);

        if (room != null) {
            ensureParticipants(room, currentUser.getId(), createOrGetDirectRoomRequest.getTargetUserId());
            return chatMapper.toRoomResponseDto(
                    room,
                    roomParticipantRepository.findByRoom_Id(room.getId())
            );
        }

        try {
            var newRoom = new Room();

            newRoom.setRoomType(RoomType.DIRECT);
            newRoom.setDirectKey(directKey);
            newRoom.setName(null);
            newRoom.setLastMessageId(null);
            newRoom.setLastMessageAt(null);

            var savedRoom = roomRepository.save(newRoom);

            addParticipant(savedRoom, currentUser.getId());
            addParticipant(savedRoom, createOrGetDirectRoomRequest.getTargetUserId());

            // Websocket
            eventPublisher.publishEvent(
                    new RoomCreatedEvent(
                            savedRoom,
                            roomParticipantRepository.findByRoom_Id(savedRoom.getId())
                    )
            );

            return chatMapper.toRoomResponseDto(
                    savedRoom,
                    roomParticipantRepository.findByRoom_Id(savedRoom.getId())
            );
        } catch (DataIntegrityViolationException ex) {
            Room existed = roomRepository.findByDirectKey(directKey).orElseThrow(() -> ex);
            ensureParticipants(existed, currentUser.getId(), createOrGetDirectRoomRequest.getTargetUserId());
            return chatMapper.toRoomResponseDto(existed, roomParticipantRepository.findByRoom_Id(existed.getId()));
        }
    }

    /* ========================================================================== */
    /*                         TẠO/QUẢN LÝ PHÒNG CHAT GROUP                        */
    /* ========================================================================== */
    @Override
    public RoomResponse createGroupRoom(CreateGroupRoomRequest createGroupRoomRequest) {
        var currentUser = currentUserProvider.get();
        List<Long> memberIds = createGroupRoomRequest.getMemberIds();

        if (memberIds == null || memberIds.size() < 2)
            throw new IllegalArgumentException("Nhóm phải có ít nhất 3 người bao gồm bạn");

        if (memberIds.contains(currentUser.getId()))
            throw new IllegalArgumentException("Không cần thêm chính mình vào danh sách");

        // Kiểm tra người dùng có tồn tại
        List<Long> invalidIds = memberIds.stream()
                .filter(id -> !userRepository.existsById(id))
                .toList();
        if (!invalidIds.isEmpty())
            throw new IllegalArgumentException("Một số người dùng không tồn tại: " + invalidIds);

        // Tạo phòng
        Room room = new Room();
        room.setRoomType(RoomType.GROUP);
        room.setName(createGroupRoomRequest.getName());
        room.setDirectKey(null);
        room.setLastMessageId(null);
        room.setLastMessageAt(null);

        var savedRoom = roomRepository.save(room);

        // Thêm người tạo nhóm (admin)
        RoomMemberId ownerPk = new RoomMemberId(savedRoom.getId(), currentUser.getId());
        RoomParticipant owner = new RoomParticipant();
        owner.setId(ownerPk);
        owner.setRoom(savedRoom);
        owner.setUser(currentUser);
        owner.setRole(RoomRole.OWNER);
        roomParticipantRepository.save(owner);

        // Thêm các thành viên khác
        memberIds.forEach(userId -> addParticipant(savedRoom, userId));
        groupSettingsRepository.save(defaultGroupSettings(savedRoom));

        // Websocket
        eventPublisher.publishEvent(
                new RoomCreatedEvent(
                        savedRoom,
                        roomParticipantRepository.findByRoom_Id(savedRoom.getId())
                )
        );

        return chatMapper.toRoomResponseDto(
                savedRoom,
                roomParticipantRepository.findByRoom_Id(savedRoom.getId())
        );
    }

    @Override
    public RoomResponse addGroupMembers(AddGroupMembersRequest request) {
        var currentUser = currentUserProvider.get();

        var room = getGroupRoom(request.getRoomId());

        // --------------------------------------------------------------------------------
        // Phân quyền
        // OWNER và ADMIN có quyền thêm thành viên mới
        // MEMBER không có quyền thêm thành viên mới
        // --------------------------------------------------------------------------------
        var caller = getParticipant(room.getId(), currentUser.getId(), "Bạn không thuộc phòng");

        var settings = loadOrCreateGroupSettings(room);
        boolean memberCanOnlyRequest = caller.getRole() == RoomRole.MEMBER
                && Boolean.TRUE.equals(settings.getJoinApprovalEnabled());

        if (caller.getRole() == RoomRole.MEMBER && !memberCanOnlyRequest) {
            throw new IllegalArgumentException("Bạn không có quyền thêm thành viên");
        }
        // --------------------------------------------------------------------------------

        // Lọc thành viên không tồn tại
        var invalidIds = request.getMemberIds().stream()
                .filter(id -> !userRepository.existsById(id))
                .toList();

        if (!invalidIds.isEmpty())
            throw new IllegalArgumentException("Người dùng không tồn tại: " + invalidIds);

        if (memberCanOnlyRequest) {
            var members = roomParticipantRepository.findByRoom_Id(room.getId());

            for (Long targetUserId : request.getMemberIds()) {
                var targetPk = new RoomMemberId(room.getId(), targetUserId);
                if (roomParticipantRepository.existsById(targetPk)) continue;

                var targetUser = userRepository.getReferenceById(targetUserId);
                var joinRequest = groupJoinRequestRepository
                        .findByRoom_IdAndRequester_Id(room.getId(), targetUserId)
                        .orElseGet(() -> {
                            var req = new GroupJoinRequest();
                            req.setRoom(room);
                            req.setRequester(targetUser);
                            return req;
                        });

                joinRequest.setStatus(GroupJoinRequestStatus.PENDING);
                joinRequest.setReviewedAt(null);
                joinRequest.setReviewedByUserId(null);
                groupJoinRequestRepository.save(joinRequest);

                String content = currentUser.getName() +
                        " đã gửi yêu cầu thêm " +
                        targetUser.getName() +
                        " vào nhóm (chờ duyệt)";
                var sysMsg = messageService.createSystemMessage(room, content, currentUser);
                eventPublisher.publishEvent(new RoomUpdatedEvent(room, members, sysMsg));
            }

            return chatMapper.toRoomResponseDto(
                    room,
                    roomParticipantRepository.findByRoom_Id(room.getId())
            );
        }

        // Duyệt qua danh sách thêm thành viên vào Room
        for (Long targetUserId : request.getMemberIds()) {

            addParticipant(room, targetUserId);

            var members = roomParticipantRepository.findByRoom_Id(room.getId());
            var targetUser = userRepository.getReferenceById(targetUserId);

            String content = currentUser.getName() +
                    " đã thêm " +
                    targetUser.getName() +
                    " vào nhóm";

            // --------------------------------------------------------------------------------
            // Websocket
            // + Bắn sự kiện tạo SYSTEM MESSAGE
            // + Bắn sự kiện cập nhật phòng
            // --------------------------------------------------------------------------------
            var sysMsg = messageService.createSystemMessage(room, content, currentUser);

            eventPublisher.publishEvent(
                    new RoomMemberAddedEvent(
                            room,
                            members,
                            targetUserId,
                            currentUser.getId(),
                            sysMsg
                    )
            );
        }

        return chatMapper.toRoomResponseDto(
                room,
                roomParticipantRepository.findByRoom_Id(room.getId())
        );
    }

    @Override
    public RoomResponse removeGroupMember(Long roomId, Long targetUserId) {
        var currentUser = currentUserProvider.get();

        var room = getGroupRoom(roomId);

        if (currentUser.getId().equals(targetUserId))
            throw new IllegalArgumentException("Không thể tự xóa chính mình");

        var caller = getParticipant(roomId, currentUser.getId(), "Bạn không thuộc phòng");
        var target = getParticipant(roomId, targetUserId, "Người dùng không thuộc phòng");

        // --------------------------------------------------------------------------------
        // Phân quyền
        // OWNER: được remove thành viên có role ADMIN hoặc MEMBER
        // ADMIN: được remove thành viên có role MEMBER
        // MEMBER: không remove được ai
        // --------------------------------------------------------------------------------

        requireCanRemoveMember(caller, target);

        // --------------------------------------------------------------------------------
        // Xóa thành viên
        // --------------------------------------------------------------------------------
        roomParticipantRepository.delete(target);
        var members = roomParticipantRepository.findByRoom_Id(room.getId());

        // --------------------------------------------------------------------------------
        // Websocket
        // + Bắn sự kiện tạo SYSTEM MESSAGE
        // + Bắn sự kiện cập nhật phòng
        // --------------------------------------------------------------------------------
        String content = currentUser.getName() +
                " đã xóa " +
                target.getUser().getName() +
                " khỏi nhóm";

        var sysMsg = messageService.createSystemMessage(room, content, currentUser);
        eventPublisher.publishEvent(
                new RoomMemberRemovedEvent(
                        room,
                        members,
                        targetUserId,
                        currentUser.getId(),
                        sysMsg
                )
        );

        return chatMapper.toRoomResponseDto(
                room,
                members
        );
    }

    @Override
    public RoomResponse leaveGroup(Long roomId, LeaveGroupRequest request) {
        var currentUser = currentUserProvider.get();
        var room = getGroupRoom(roomId);

        var caller = getParticipant(roomId, currentUser.getId(), "Bạn không thuộc nhóm");
        var participantsBeforeLeave = roomParticipantRepository.findByRoom_Id(roomId);

        // If owner is the last remaining member and leaves, auto-dissolve the group.
        if (caller.getRole() == RoomRole.OWNER && participantsBeforeLeave.size() == 1) {
            dissolveGroupInternal(room, participantsBeforeLeave, currentUser.getId());
            return null;
        }

        if (caller.getRole() == RoomRole.OWNER) {
            Long newOwnerId = request == null ? null : request.getNewOwnerId();
            if (newOwnerId == null)
                throw new IllegalArgumentException("Trưởng nhóm cần chọn trưởng nhóm mới trước khi rời nhóm");

            if (currentUser.getId().equals(newOwnerId))
                throw new IllegalArgumentException("Trưởng nhóm mới phải là thành viên khác");

            var newOwner = getParticipant(roomId, newOwnerId, "Trưởng nhóm mới không thuộc nhóm");
            RoomRole oldRole = newOwner.getRole();
            newOwner.setRole(RoomRole.OWNER);
            roomParticipantRepository.save(newOwner);

            var transferMsg = messageService.createSystemMessage(
                    room,
                    currentUser.getName() + " đã chuyển quyền trưởng nhóm cho " + newOwner.getUser().getName(),
                    currentUser
            );

            var membersAfterTransfer = roomParticipantRepository.findByRoom_Id(roomId);
            eventPublisher.publishEvent(new RoomMemberRoleChangedEvent(
                    room,
                    membersAfterTransfer,
                    newOwnerId,
                    oldRole,
                    RoomRole.OWNER,
                    currentUser.getId(),
                    transferMsg
            ));
        }

        roomParticipantRepository.delete(caller);

        var membersAfterLeave = roomParticipantRepository.findByRoom_Id(roomId);
        var leaveMsg = messageService.createSystemMessage(
                room,
                currentUser.getName() + " đã rời khỏi nhóm",
                currentUser
        );

        eventPublisher.publishEvent(new RoomMemberRemovedEvent(
                room,
                membersAfterLeave,
                currentUser.getId(),
                currentUser.getId(),
                leaveMsg
        ));

        return chatMapper.toRoomResponseDto(room, membersAfterLeave);
    }

    @Override
    public void dissolveGroup(Long roomId) {
        var currentUser = currentUserProvider.get();
        var room = getGroupRoom(roomId);
        var caller = getParticipant(roomId, currentUser.getId(), "Bạn không thuộc nhóm");

        requireOwner(caller, "Chỉ trưởng nhóm mới có quyền giải tán nhóm");

        var participants = roomParticipantRepository.findByRoom_Id(roomId);
        dissolveGroupInternal(room, participants, currentUser.getId());
    }

    @Override
    public RoomResponse changeMemberRole(Long roomId, Long targetUserId, RoomRole newRole) {
        var currentUser = currentUserProvider.get();

        var room = getGroupRoom(roomId);

        // ------------------------------------------------------
        // Kiểm tra caller
        // ------------------------------------------------------
        var caller = getParticipant(roomId, currentUser.getId(), "Bạn không thuộc nhóm");
        requireOwner(caller, "Chỉ Owner mới có quyền đổi role");

        // ------------------------------------------------------
        // Target
        // ------------------------------------------------------
        var target = getParticipant(roomId, targetUserId, "Người dùng không thuộc nhóm");

        var oldRole = target.getRole();

        if (oldRole == newRole)
            throw new IllegalArgumentException("Người dùng đã có role này");

        // ------------------------------------------------------
        // Update role
        // ------------------------------------------------------
        if (newRole == RoomRole.OWNER) {
            if (currentUser.getId().equals(targetUserId))
                throw new IllegalArgumentException("Bạn đã là Owner của nhóm");

            caller.setRole(RoomRole.ADMIN);
            target.setRole(RoomRole.OWNER);
            roomParticipantRepository.save(caller);
            roomParticipantRepository.save(target);
        } else {
            if (oldRole == RoomRole.OWNER && currentUser.getId().equals(targetUserId))
                throw new IllegalArgumentException("Owner không thể tự hạ quyền. Hãy chuyển Owner cho người khác");

            target.setRole(newRole);
            roomParticipantRepository.save(target);
        }

        var members = roomParticipantRepository.findByRoom_Id(roomId);

        // ------------------------------------------------------
        // System message: "A đã đổi role của B thành ADMIN"
        // ------------------------------------------------------
        String content = currentUser.getName() +
                " đã đổi quyền của " +
                target.getUser().getName() +
                " thành " + newRole.name();

        var sysMsg = messageService.createSystemMessage(room, content, currentUser);

        // ------------------------------------------------------
        // WS
        // ------------------------------------------------------
        eventPublisher.publishEvent(
                new RoomMemberRoleChangedEvent(
                        room,
                        members,
                        targetUserId,
                        oldRole,
                        newRole,
                        currentUser.getId(),
                        sysMsg
                )
        );

        return chatMapper.toRoomResponseDto(room, members);
    }

    @Override
    public RoomResponse renameGroup(Long roomId, String newName) {
        var currentUser = currentUserProvider.get();

        var room = getGroupRoom(roomId);
        var settings = loadOrCreateGroupSettings(room);

        // --------------------------
        // Kiểm tra role
        // --------------------------
        var caller = getParticipant(roomId, currentUser.getId(), "Bạn không thuộc nhóm");
        requireCanEditGroupProfile(caller, settings, "Bạn không có quyền đổi tên nhóm");

        // --------------------------
        // Update tên nhóm
        // --------------------------
        room.setName(newName);
        roomRepository.save(room);

        var members = roomParticipantRepository.findByRoom_Id(roomId);

        // --------------------------
        // System message
        // --------------------------
        String content = currentUser.getName() +
                " đã đổi tên nhóm thành \"" + newName + "\"";

        var sysMsg = messageService.createSystemMessage(room, content, currentUser);

        // --------------------------
        // WS
        // --------------------------
        eventPublisher.publishEvent(
                new RoomUpdatedEvent(
                        room,
                        members,
                        sysMsg
                )
        );

        return chatMapper.toRoomResponseDto(room, members);
    }

    @Override
    public RoomResponse updateGroupImage(Long roomId, MultipartFile file) {
        var currentUser = currentUserProvider.get();

        var room = getGroupRoom(roomId);
        var settings = loadOrCreateGroupSettings(room);

        var caller = getParticipant(roomId, currentUser.getId(), "Bạn không thuộc nhóm");
        requireCanEditGroupProfile(caller, settings, "Bạn không có quyền đổi ảnh nhóm");

        var members = roomParticipantRepository.findByRoom_Id(roomId);

        // ================================================
        // XÓA ẢNH (file == null)
        // ================================================
        if (file == null) {
            if (room.getRoomImgUrl() != null) {
                s3Service.deleteFileByUrl(room.getRoomImgUrl());
            }

            room.setRoomImgUrl(null);
            roomRepository.save(room);

            var sysMsg = messageService.createSystemMessage(
                    room,
                    currentUser.getName() + " đã xoá ảnh nhóm",
                    currentUser
            );

            eventPublisher.publishEvent(new RoomUpdatedEvent(room, members, sysMsg));

            return chatMapper.toRoomResponseDto(room, members);
        }

        // ================================================
        // UPLOAD ẢNH MỚI – RANDOM FILE NAME Ở ĐÂY
        // ================================================
        String original = file.getOriginalFilename();
        String ext;

        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));  // .png, .jpg
        } else {
            ext = ".png";  // fallback
        }

        // Tên random:
        String randomFileName = UUID.randomUUID() + ext;

        String newUrl = s3Service.uploadFile(
                file,
                "group-images",
                randomFileName,
                true,
                MAX_IMAGE_SIZE
        );

        // Xóa ảnh cũ
        if (room.getRoomImgUrl() != null) {
            s3Service.deleteFileByUrl(room.getRoomImgUrl());
        }

        // Cập nhật DB
        room.setRoomImgUrl(newUrl);
        roomRepository.save(room);

        var sysMsg = messageService.createSystemMessage(
                room,
                currentUser.getName() + " đã cập nhật ảnh nhóm",
                currentUser
        );

        eventPublisher.publishEvent(new RoomUpdatedEvent(room, members, sysMsg));

        return chatMapper.toRoomResponseDto(room, members);
    }

    /* ========================================================================== */
    /*                         TÙY CHỈNH PHÒNG CHAT                               */
    /* ========================================================================== */
    @Override
    public RoomResponse changeTheme(Long roomId, String newTheme) {
        var currentUser = currentUserProvider.get();

        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));

        // ------------------------------------------------------
        // DIRECT ROOM → ai cũng được đổi theme
        // ------------------------------------------------------
        if (room.getRoomType() == RoomType.DIRECT) {

            room.setTheme(newTheme);
            roomRepository.save(room);

            var members = roomParticipantRepository.findByRoom_Id(roomId);

            // DIRECT không tạo system message
            eventPublisher.publishEvent(
                    new RoomUpdatedEvent(room, members, null)
            );

            return chatMapper.toRoomResponseDto(room, members);
        }

        // ------------------------------------------------------
        // GROUP ROOM → cần kiểm tra quyền
        // ------------------------------------------------------
        var caller = getParticipant(roomId, currentUser.getId(), "Bạn không thuộc nhóm");
        requireOwnerOrAdmin(caller, "Bạn không có quyền đổi theme nhóm");

        // ------------------------------------------------------
        // Update theme
        // ------------------------------------------------------
        room.setTheme(newTheme);
        roomRepository.save(room);

        var members = roomParticipantRepository.findByRoom_Id(roomId);

        // ------------------------------------------------------
        // System message cho GROUP
        // ------------------------------------------------------
        String content = currentUser.getName() +
                " đã đổi chủ đề nhóm thành \"" + newTheme + "\"";

        var sysMsg = messageService.createSystemMessage(room, content, currentUser);

        // ------------------------------------------------------
        // Broadcast WS
        // ------------------------------------------------------
        eventPublisher.publishEvent(
                new RoomUpdatedEvent(room, members, sysMsg)
        );

        return chatMapper.toRoomResponseDto(room, members);
    }


    /* ========================================================================== */
    /*                         LẤY LỊCH SỬ PHÒNG CHAT                             */
    /* ========================================================================== */
    @Override
    public Page<RoomResponse> getCurrentUserRooms(Pageable pageable) {
        var currentUser = currentUserProvider.get();
        var currentUserId = currentUser.getId();

        Page<Room> page = roomRepository.findAllByMember(currentUserId, pageable);

        if (page.isEmpty()) return Page.empty(pageable);

        List<Long> roomIds = page.getContent().stream().map(Room::getId).toList();
        Map<Long, List<RoomParticipant>> participantsByRoom = roomParticipantRepository
                .findByRoom_IdIn(roomIds)
                .stream()
                .collect(Collectors.groupingBy(rp -> rp.getRoom().getId()));


        List<RoomResponse> content = page
                .getContent()
                .stream()
                .map(room -> {
                    var members = participantsByRoom.getOrDefault(room.getId(), List.of());
                    return chatMapper.toRoomResponseDto(room, members);
                })
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Override
    public GroupSettingsResponse getGroupSettings(Long roomId) {
        var currentUser = currentUserProvider.get();
        getParticipant(roomId, currentUser.getId(), "Bạn không thuộc nhóm");
        return toGroupSettingsResponse(loadOrCreateGroupSettings(getGroupRoom(roomId)));
    }

    @Override
    public GroupSettingsResponse updateGroupSettings(Long roomId, UpdateGroupSettingsRequest request) {
        var currentUser = currentUserProvider.get();
        var room = getGroupRoom(roomId);
        var caller = getParticipant(roomId, currentUser.getId(), "Bạn không thuộc nhóm");
        requireOwnerOrAdmin(caller, "Bạn không có quyền cập nhật cài đặt nhóm");

        var settings = loadOrCreateGroupSettings(room);
        var changedMessages = new ArrayList<String>();

        if (request.getAllowMemberEditGroupProfile() != null
                && !request.getAllowMemberEditGroupProfile().equals(settings.getAllowMemberEditGroupProfile())) {
            settings.setAllowMemberEditGroupProfile(request.getAllowMemberEditGroupProfile());
            changedMessages.add("quyền đổi tên và ảnh đại diện nhóm cho thành viên");
        }
        if (request.getAllowMemberPinMessage() != null
                && !request.getAllowMemberPinMessage().equals(settings.getAllowMemberPinMessage())) {
            settings.setAllowMemberPinMessage(request.getAllowMemberPinMessage());
            changedMessages.add("quyền ghim tin nhắn cho thành viên");
        }
        if (request.getAllowMemberCreatePoll() != null
                && !request.getAllowMemberCreatePoll().equals(settings.getAllowMemberCreatePoll())) {
            settings.setAllowMemberCreatePoll(request.getAllowMemberCreatePoll());
            changedMessages.add("quyền tạo bình chọn cho thành viên");
        }
        if (request.getAllowMemberSendMessage() != null
                && !request.getAllowMemberSendMessage().equals(settings.getAllowMemberSendMessage())) {
            settings.setAllowMemberSendMessage(request.getAllowMemberSendMessage());
            changedMessages.add("quyền gửi tin nhắn cho thành viên");
        }
        if (request.getJoinApprovalEnabled() != null
                && !request.getJoinApprovalEnabled().equals(settings.getJoinApprovalEnabled())) {
            settings.setJoinApprovalEnabled(request.getJoinApprovalEnabled());
            changedMessages.add("chế độ phê duyệt thành viên mới");
        }
        if (request.getHighlightAdminMessageOnly() != null
                && !request.getHighlightAdminMessageOnly().equals(settings.getHighlightAdminMessageOnly())) {
            settings.setHighlightAdminMessageOnly(request.getHighlightAdminMessageOnly());
            changedMessages.add("đánh dấu tin nhắn từ trưởng/phó nhóm");
        }
        if (request.getAllowNewMemberReadRecent() != null
                && !request.getAllowNewMemberReadRecent().equals(settings.getAllowNewMemberReadRecent())) {
            settings.setAllowNewMemberReadRecent(request.getAllowNewMemberReadRecent());
            changedMessages.add("quyền đọc tin nhắn gần nhất cho thành viên mới");
        }
        if (request.getJoinLinkEnabled() != null
                && !request.getJoinLinkEnabled().equals(settings.getJoinLinkEnabled())) {
            settings.setJoinLinkEnabled(request.getJoinLinkEnabled());
            changedMessages.add("trạng thái link tham gia nhóm");
        }

        groupSettingsRepository.save(settings);

        if (!changedMessages.isEmpty()) {
            var members = roomParticipantRepository.findByRoom_Id(room.getId());
            for (String changedItem : changedMessages) {
                String content = currentUser.getName() + " đã cập nhật " + changedItem;
                var sysMsg = messageService.createSystemMessage(room, content, currentUser);
                eventPublisher.publishEvent(new RoomUpdatedEvent(room, members, sysMsg));
            }
        }

        return toGroupSettingsResponse(settings);
    }

    @Override
    public GroupSettingsResponse regenerateGroupJoinLink(Long roomId) {
        var currentUser = currentUserProvider.get();
        var room = getGroupRoom(roomId);
        var caller = getParticipant(roomId, currentUser.getId(), "Bạn không thuộc nhóm");
        requireOwnerOrAdmin(caller, "Bạn không có quyền tạo lại link nhóm");

        var settings = loadOrCreateGroupSettings(room);
        settings.setJoinLinkToken(generateJoinLinkToken());
        settings.setJoinLinkEnabled(true);

        groupSettingsRepository.save(settings);
        return toGroupSettingsResponse(settings);
    }

    @Override
    public JoinGroupByLinkResponse joinGroupByLink(JoinGroupByLinkRequest request) {
        var currentUser = currentUserProvider.get();
        var settings = groupSettingsRepository.findByJoinLinkToken(request.getJoinLinkToken())
                .orElseThrow(() -> new IllegalArgumentException("Link tham gia nhóm không hợp lệ"));
        var room = getGroupRoom(settings.getRoomId());

        if (!Boolean.TRUE.equals(settings.getJoinLinkEnabled())) {
            throw new IllegalArgumentException("Link tham gia nhóm đã bị tắt");
        }

        var pk = new RoomMemberId(room.getId(), currentUser.getId());
        if (roomParticipantRepository.existsById(pk)) {
            return new JoinGroupByLinkResponse(
                    true,
                    "Bạn đã là thành viên của nhóm",
                    chatMapper.toRoomResponseDto(room, roomParticipantRepository.findByRoom_Id(room.getId())),
                    null
            );
        }

        if (!Boolean.TRUE.equals(settings.getJoinApprovalEnabled())) {
            addParticipant(room, currentUser.getId());
            var members = roomParticipantRepository.findByRoom_Id(room.getId());
            var sysMsg = messageService.createSystemMessage(
                    room,
                    currentUser.getName() + " đã tham gia nhóm bằng link",
                    currentUser
            );

            eventPublisher.publishEvent(
                    new RoomMemberAddedEvent(
                            room,
                            members,
                            currentUser.getId(),
                            currentUser.getId(),
                            sysMsg
                    )
            );

            return new JoinGroupByLinkResponse(
                    true,
                    "Tham gia nhóm thành công",
                    chatMapper.toRoomResponseDto(room, members),
                    null
            );
        }

        var joinRequest = groupJoinRequestRepository
                .findByRoom_IdAndRequester_Id(room.getId(), currentUser.getId())
                .orElseGet(() -> {
                    var req = new GroupJoinRequest();
                    req.setRoom(room);
                    req.setRequester(currentUser);
                    req.setStatus(GroupJoinRequestStatus.PENDING);
                    return req;
                });

        joinRequest.setStatus(GroupJoinRequestStatus.PENDING);
        joinRequest.setReviewedAt(null);
        joinRequest.setReviewedByUserId(null);
        joinRequest = groupJoinRequestRepository.save(joinRequest);

        return new JoinGroupByLinkResponse(
                false,
                "Yêu cầu tham gia nhóm đã được gửi, vui lòng chờ duyệt",
                null,
                toGroupJoinRequestResponse(joinRequest)
        );
    }

    @Override
    public List<GroupJoinRequestResponse> getGroupJoinRequests(Long roomId, GroupJoinRequestStatus status) {
        var currentUser = currentUserProvider.get();
        var caller = getParticipant(roomId, currentUser.getId(), "Bạn không thuộc nhóm");
        requireOwnerOrAdmin(caller, "Bạn không có quyền xem yêu cầu vào nhóm");

        List<GroupJoinRequest> requests = status == null
                ? groupJoinRequestRepository.findByRoom_IdOrderByCreatedAtDesc(roomId)
                : groupJoinRequestRepository.findByRoom_IdAndStatusOrderByCreatedAtDesc(roomId, status);

        return requests.stream()
                .map(this::toGroupJoinRequestResponse)
                .toList();
    }

    @Override
    public GroupJoinRequestResponse reviewGroupJoinRequest(Long roomId, Long joinRequestId, ReviewGroupJoinRequest request) {
        var currentUser = currentUserProvider.get();
        var room = getGroupRoom(roomId);
        var caller = getParticipant(roomId, currentUser.getId(), "Bạn không thuộc nhóm");
        requireOwnerOrAdmin(caller, "Bạn không có quyền duyệt thành viên");

        var joinRequest = groupJoinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Yêu cầu tham gia không tồn tại"));

        if (!joinRequest.getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("Yêu cầu tham gia không thuộc nhóm này");
        }

        if (joinRequest.getStatus() != GroupJoinRequestStatus.PENDING) {
            throw new IllegalArgumentException("Yêu cầu tham gia đã được xử lý");
        }

        User requester = joinRequest.getRequester();
        if (Boolean.TRUE.equals(request.getApproved())) {
            addParticipant(room, requester.getId());
            joinRequest.setStatus(GroupJoinRequestStatus.APPROVED);

            var members = roomParticipantRepository.findByRoom_Id(room.getId());
            String content = currentUser.getName() + " đã duyệt " + requester.getName() + " vào nhóm";
            var sysMsg = messageService.createSystemMessage(room, content, currentUser);
            eventPublisher.publishEvent(
                    new RoomMemberAddedEvent(
                            room,
                            members,
                            requester.getId(),
                            currentUser.getId(),
                            sysMsg
                    )
            );
        } else {
            joinRequest.setStatus(GroupJoinRequestStatus.REJECTED);
        }

        joinRequest.setReviewedByUserId(currentUser.getId());
        joinRequest.setReviewedAt(java.time.LocalDateTime.now());
        joinRequest = groupJoinRequestRepository.save(joinRequest);

        return toGroupJoinRequestResponse(joinRequest);
    }

    @Override
    public GroupJoinRequestResponse cancelMyGroupJoinRequest(Long roomId) {
        var currentUser = currentUserProvider.get();
        var room = getGroupRoom(roomId);

        var joinRequest = groupJoinRequestRepository
                .findByRoom_IdAndRequester_Id(room.getId(), currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa gửi yêu cầu tham gia nhóm này"));

        if (joinRequest.getStatus() != GroupJoinRequestStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể thu hồi yêu cầu đang chờ duyệt");
        }

        joinRequest.setStatus(GroupJoinRequestStatus.CANCELED);
        joinRequest.setReviewedByUserId(currentUser.getId());
        joinRequest.setReviewedAt(java.time.LocalDateTime.now());
        joinRequest = groupJoinRequestRepository.save(joinRequest);

        return toGroupJoinRequestResponse(joinRequest);
    }

    /* ========================================================================== */
    /*                           CÁC HÀM HỖ TRỢ KHÁC                              */
    /* ========================================================================== */
    private String buildDirectKey(Long a, Long b) {
        long low = Math.min(a, b);
        long high = Math.max(a, b);
        return low + "_" + high;
    }

    private void ensureParticipants(Room room, Long currentUserId, Long targetUserId) {
        addParticipant(room, currentUserId);
        addParticipant(room, targetUserId);
    }

    private Room getGroupRoom(Long roomId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));

        if (room.getRoomType() != RoomType.GROUP)
            throw new IllegalArgumentException("Chỉ phòng nhóm mới được thực hiện thao tác này");

        return room;
    }

    private RoomParticipant getParticipant(Long roomId, Long userId, String errorMessage) {
        var memberId = new RoomMemberId(roomId, userId);
        return roomParticipantRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }

    private void requireOwner(RoomParticipant caller, String errorMessage) {
        if (caller.getRole() != RoomRole.OWNER)
            throw new IllegalArgumentException(errorMessage);
    }

    private void requireOwnerOrAdmin(RoomParticipant caller, String errorMessage) {
        if (caller.getRole() == RoomRole.MEMBER)
            throw new IllegalArgumentException(errorMessage);
    }

    private void requireCanRemoveMember(RoomParticipant caller, RoomParticipant target) {
        if (caller.getRole() == RoomRole.MEMBER)
            throw new IllegalArgumentException("Bạn không có quyền xóa thành viên");

        if (target.getRole() == RoomRole.OWNER)
            throw new IllegalArgumentException("Không thể xóa Owner khỏi nhóm");

        if (caller.getRole() == RoomRole.ADMIN && target.getRole() != RoomRole.MEMBER)
            throw new IllegalArgumentException("Admin chỉ được xóa Member");
    }

    private void requireCanEditGroupProfile(RoomParticipant caller, GroupSettings settings, String errorMessage) {
        if (caller.getRole() == RoomRole.MEMBER
                && !Boolean.TRUE.equals(settings.getAllowMemberEditGroupProfile())) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void addParticipant(Room room, Long userId) {
        RoomMemberId pk = new RoomMemberId(room.getId(), userId);
        if (roomParticipantRepository.existsById(pk)) return;

        RoomParticipant rp = new RoomParticipant();
        rp.setId(pk);
        rp.setRoom(room);
        rp.setUser(userRepository.getReferenceById(userId));
        rp.setRole(RoomRole.MEMBER);

        try {
            roomParticipantRepository.save(rp);
        } catch (DataIntegrityViolationException ignored) {

        }
    }

    private void dissolveGroupInternal(Room room, List<RoomParticipant> participants, Long actorUserId) {
        Long roomId = room.getId();
        // "Delete for me" records keep FK(room_id -> rooms.id). Must clean them first.
        deletedMessageRepository.deleteByIdRoomId(roomId);
        groupSettingsRepository.deleteById(roomId);
        roomParticipantRepository.deleteAll(participants);
        roomRepository.delete(room);

        eventPublisher.publishEvent(new RoomDeletedEvent(
                room,
                participants,
                actorUserId
        ));
    }

    private GroupSettings loadOrCreateGroupSettings(Room room) {
        return groupSettingsRepository.findByRoomId(room.getId())
                .orElseGet(() -> groupSettingsRepository.save(defaultGroupSettings(room)));
    }

    private GroupSettings defaultGroupSettings(Room room) {
        var settings = new GroupSettings();
        settings.setRoomId(room.getId());
        settings.setAllowMemberEditGroupProfile(false);
        settings.setAllowMemberPinMessage(false);
        settings.setAllowMemberCreatePoll(false);
        settings.setAllowMemberSendMessage(true);
        settings.setJoinApprovalEnabled(false);
        settings.setHighlightAdminMessageOnly(false);
        settings.setAllowNewMemberReadRecent(true);
        settings.setJoinLinkEnabled(true);
        settings.setJoinLinkToken(generateJoinLinkToken());
        return settings;
    }

    private String generateJoinLinkToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private GroupSettingsResponse toGroupSettingsResponse(GroupSettings settings) {
        String joinLink = Boolean.TRUE.equals(settings.getJoinLinkEnabled())
                ? "pingme.me/g/" + settings.getJoinLinkToken()
                : null;

        return new GroupSettingsResponse(
                settings.getRoomId(),
                settings.getAllowMemberEditGroupProfile(),
                settings.getAllowMemberPinMessage(),
                settings.getAllowMemberCreatePoll(),
                settings.getAllowMemberSendMessage(),
                settings.getJoinApprovalEnabled(),
                settings.getHighlightAdminMessageOnly(),
                settings.getAllowNewMemberReadRecent(),
                settings.getJoinLinkEnabled(),
                joinLink
        );
    }

    private GroupJoinRequestResponse toGroupJoinRequestResponse(GroupJoinRequest request) {
        return new GroupJoinRequestResponse(
                request.getId(),
                request.getRoom().getId(),
                request.getRequester().getId(),
                request.getRequester().getName(),
                request.getRequester().getAvatarUrl(),
                request.getStatus(),
                request.getReviewedByUserId(),
                request.getReviewedAt(),
                request.getCreatedAt()
        );
    }

}






