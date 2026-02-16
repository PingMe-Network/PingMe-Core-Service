package me.huynhducphu.ping_me.service.chat.impl;

import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.request.chat.room.AddGroupMembersRequest;
import me.huynhducphu.ping_me.dto.request.chat.room.CreateGroupRoomRequest;
import me.huynhducphu.ping_me.dto.request.chat.room.CreateOrGetDirectRoomRequest;
import me.huynhducphu.ping_me.dto.response.chat.room.RoomResponse;
import me.huynhducphu.ping_me.model.chat.Room;
import me.huynhducphu.ping_me.model.chat.RoomParticipant;
import me.huynhducphu.ping_me.model.common.RoomMemberId;
import me.huynhducphu.ping_me.model.constant.RoomRole;
import me.huynhducphu.ping_me.model.constant.RoomType;
import me.huynhducphu.ping_me.repository.jpa.auth.UserRepository;
import me.huynhducphu.ping_me.repository.jpa.chat.RoomParticipantRepository;
import me.huynhducphu.ping_me.repository.jpa.chat.RoomRepository;
import me.huynhducphu.ping_me.service.chat.MessageService;
import me.huynhducphu.ping_me.service.chat.event.*;
import me.huynhducphu.ping_me.service.s3.S3Service;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import me.huynhducphu.ping_me.utils.mapper.ChatMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
public class RoomServiceImpl implements me.huynhducphu.ping_me.service.chat.RoomService {

    // SERVICE
    private final MessageService messageService;
    private final S3Service s3Service;

    // PROVIDER
    private final CurrentUserProvider currentUserProvider;

    // REPOSITORY
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
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

        var room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));

        if (room.getRoomType() != RoomType.GROUP)
            throw new IllegalArgumentException("Chỉ được thêm thành viên vào phòng nhóm");

        // --------------------------------------------------------------------------------
        // Phân quyền
        // OWNER và ADMIN có quyền thêm thành viên mới
        // MEMBER không có quyền thêm thành viên mới
        // --------------------------------------------------------------------------------
        var callerPk = new RoomMemberId(room.getId(), currentUser.getId());
        var caller = roomParticipantRepository.findById(callerPk)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không thuộc phòng"));

        if (caller.getRole() == RoomRole.MEMBER)
            throw new IllegalArgumentException("Bạn không có quyền thêm thành viên");
        // --------------------------------------------------------------------------------

        // Lọc thành viên không tồn tại
        var invalidIds = request.getMemberIds().stream()
                .filter(id -> !userRepository.existsById(id))
                .toList();

        if (!invalidIds.isEmpty())
            throw new IllegalArgumentException("Người dùng không tồn tại: " + invalidIds);

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
            // + Bắn sự kiện tạo SYSTEM MESSAGGE
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

        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));

        if (room.getRoomType() != RoomType.GROUP)
            throw new IllegalArgumentException("Chỉ phòng nhóm mới được xóa thành viên");

        if (currentUser.getId().equals(targetUserId))
            throw new IllegalArgumentException("Không thể tự xóa chính mình này");

        var callerPk = new RoomMemberId(roomId, currentUser.getId());
        var caller = roomParticipantRepository.findById(callerPk)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không thuộc phòng"));

        var targetPk = new RoomMemberId(roomId, targetUserId);
        var target = roomParticipantRepository.findById(targetPk)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không thuộc phòng"));

        // --------------------------------------------------------------------------------
        // Phân quyền
        // OWNER: được remove thành viên có role ADMIN hoặc MEMBER
        // ADMIN: được remove thành viên có role MEMBER
        // MEMBER: không remove được ai
        // --------------------------------------------------------------------------------

        if (caller.getRole() == RoomRole.MEMBER)
            throw new IllegalArgumentException("Bạn không có quyền xóa thành viên");

        if (caller.getRole() == RoomRole.ADMIN && target.getRole() != RoomRole.MEMBER)
            throw new IllegalArgumentException("Admin chỉ được xóa Member");

        // --------------------------------------------------------------------------------
        // Xóa thành viên
        // --------------------------------------------------------------------------------
        int count = roomParticipantRepository.findByRoom_Id(room.getId()).size();
        if (count <= 3)
            throw new IllegalArgumentException("Phòng phải có ít nhất 3 thành viên");

        roomParticipantRepository.delete(target);
        var members = roomParticipantRepository.findByRoom_Id(room.getId());

        // --------------------------------------------------------------------------------
        // Websocket
        // + Bắn sự kiện tạo SYSTEM MESSAGGE
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
    public RoomResponse changeMemberRole(Long roomId, Long targetUserId, RoomRole newRole) {
        var currentUser = currentUserProvider.get();

        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));

        if (room.getRoomType() != RoomType.GROUP)
            throw new IllegalArgumentException("Chỉ phòng nhóm mới đổi quyền");

        // ------------------------------------------------------
        // Kiểm tra caller
        // ------------------------------------------------------
        var callerPk = new RoomMemberId(roomId, currentUser.getId());
        var caller = roomParticipantRepository.findById(callerPk)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không thuộc nhóm"));

        if (caller.getRole() == RoomRole.MEMBER)
            throw new IllegalArgumentException("Bạn không có quyền đổi role");

        // ------------------------------------------------------
        // Target
        // ------------------------------------------------------
        var targetPk = new RoomMemberId(roomId, targetUserId);
        var target = roomParticipantRepository.findById(targetPk)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không thuộc nhóm"));

        var oldRole = target.getRole();

        if (oldRole == newRole)
            throw new IllegalArgumentException("Người dùng đã có role này");

        // ADMIN không được chỉnh OWNER
        if (caller.getRole() == RoomRole.ADMIN && oldRole == RoomRole.OWNER)
            throw new IllegalArgumentException("Admin không thể chỉnh role Owner");

        // ------------------------------------------------------
        // Update role
        // ------------------------------------------------------
        target.setRole(newRole);
        roomParticipantRepository.save(target);

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

        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));

        if (room.getRoomType() != RoomType.GROUP)
            throw new IllegalArgumentException("Chỉ nhóm mới đổi tên");

        // --------------------------
        // Kiểm tra role
        // --------------------------
        var callerPk = new RoomMemberId(roomId, currentUser.getId());
        var caller = roomParticipantRepository.findById(callerPk)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không thuộc nhóm"));

        if (caller.getRole() == RoomRole.MEMBER)
            throw new IllegalArgumentException("Bạn không có quyền đổi tên nhóm");

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

        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));

        if (room.getRoomType() != RoomType.GROUP)
            throw new IllegalArgumentException("Chỉ group mới đổi ảnh");

        var callerPk = new RoomMemberId(roomId, currentUser.getId());
        var caller = roomParticipantRepository.findById(callerPk)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không thuộc nhóm"));

        if (caller.getRole() == RoomRole.MEMBER)
            throw new IllegalArgumentException("Bạn không có quyền đổi ảnh nhóm");

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
        var callerPk = new RoomMemberId(roomId, currentUser.getId());
        var caller = roomParticipantRepository.findById(callerPk)
                .orElseThrow(() -> new IllegalArgumentException("Bạn không thuộc nhóm"));

        if (caller.getRole() == RoomRole.MEMBER)
            throw new IllegalArgumentException("Bạn không có quyền đổi theme nhóm");

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

}
