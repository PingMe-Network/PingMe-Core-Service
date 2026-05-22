package org.ping_me.service.chat;

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
import org.ping_me.model.constant.GroupJoinRequestStatus;
import org.ping_me.model.constant.RoomRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Admin 8/25/2025
 *
 **/
public interface RoomService {
    RoomResponse createOrGetDirectRoom(CreateOrGetDirectRoomRequest createOrGetDirectRoomRequest);

    RoomResponse createGroupRoom(CreateGroupRoomRequest createGroupRoomRequest);

    RoomResponse addGroupMembers(AddGroupMembersRequest request);

    RoomResponse removeGroupMember(Long roomId, Long targetUserId);

    RoomResponse leaveGroup(Long roomId, LeaveGroupRequest request);

    void dissolveGroup(Long roomId);

    RoomResponse changeMemberRole(Long roomId, Long targetUserId, RoomRole newRole);

    RoomResponse renameGroup(Long roomId, String newName);

    RoomResponse updateGroupImage(Long roomId, MultipartFile file);

    /* ========================================================================== */
    /*                         TÙY CHỈNH PHÒNG CHAT                               */
    /* ========================================================================== */
    RoomResponse changeTheme(Long roomId, String newTheme);

    Page<RoomResponse> getCurrentUserRooms(Pageable pageable);

    GroupSettingsResponse getGroupSettings(Long roomId);

    GroupSettingsResponse updateGroupSettings(Long roomId, UpdateGroupSettingsRequest request);

    GroupSettingsResponse regenerateGroupJoinLink(Long roomId);

    JoinGroupByLinkResponse joinGroupByLink(JoinGroupByLinkRequest request);

    List<GroupJoinRequestResponse> getGroupJoinRequests(Long roomId, GroupJoinRequestStatus status);

    GroupJoinRequestResponse reviewGroupJoinRequest(Long roomId, Long joinRequestId, ReviewGroupJoinRequest request);

    GroupJoinRequestResponse cancelMyGroupJoinRequest(Long roomId);
}
