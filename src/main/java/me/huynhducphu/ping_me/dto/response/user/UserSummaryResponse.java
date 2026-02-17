package me.huynhducphu.ping_me.dto.response.user;

import lombok.*;
import me.huynhducphu.ping_me.model.constant.FriendshipStatus;
import me.huynhducphu.ping_me.model.constant.UserStatus;

/**
 * Admin 8/19/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserSummaryResponse {

    private Long id;
    private String email;
    private String name;
    private String avatarUrl;
    private UserStatus status;
    private FriendshipSummary friendshipSummary;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class FriendshipSummary {
        private Long id;
        private FriendshipStatus friendshipStatus;
    }

}
