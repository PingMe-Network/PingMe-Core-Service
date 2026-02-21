package org.ping_me.dto.response.friendship;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ping_me.dto.response.user.UserSummaryResponse;

import java.util.List;

/**
 * Admin 9/2/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HistoryFriendshipResponse {
    private List<UserSummaryResponse> userSummaryResponses;
    private Boolean hasMore;
    private Long nextBeforeId;
}
