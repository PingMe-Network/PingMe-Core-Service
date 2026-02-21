package org.ping_me.service.friendship;

import org.ping_me.dto.request.friendship.FriendInvitationRequest;
import org.ping_me.dto.response.friendship.HistoryFriendshipResponse;
import org.ping_me.dto.response.friendship.UserFriendshipStatsResponse;
import org.ping_me.model.chat.Friendship;

import java.util.List;

/**
 * Admin 8/19/2025
 **/
public interface FriendshipService {
    void sendInvitation(FriendInvitationRequest friendInvitationRequest);

    void acceptInvitation(Long friendRequestId);

    void rejectInvitation(Long friendRequestId);

    void cancelInvitation(Long friendRequestId);

    void deleteFriendship(Long friendRequestId);

    HistoryFriendshipResponse getAcceptedFriendshipHistoryList(
            Long beforeId,
            Integer size
    );

    HistoryFriendshipResponse getReceivedHistoryInvitations(
            Long beforeId,
            Integer size
    );

    HistoryFriendshipResponse getSentHistoryInvitations(
            Long beforeId,
            Integer size
    );

    UserFriendshipStatsResponse getUserFrendshipStats();

    List<Friendship> getAllFriendshipsOfCurrentUser(String email);
}
