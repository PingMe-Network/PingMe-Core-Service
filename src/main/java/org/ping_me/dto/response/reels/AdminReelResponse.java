package org.ping_me.dto.response.reels;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminReelResponse {
    private Long id;
    private String videoUrl;
    private String caption;

    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long saveCount;

    private Long userId;
    private String userName;
    private String userAvatarUrl;

    private String status;
    private String adminNote;

    private LocalDateTime createdAt;
}

