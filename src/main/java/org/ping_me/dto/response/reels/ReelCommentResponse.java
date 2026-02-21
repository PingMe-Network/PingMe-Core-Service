package org.ping_me.dto.response.reels;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ReelCommentResponse {
    private Long id;
    private String content;

    private Long reelId;
    private Boolean isReelOwner;

    private Long userId;
    private String userName;
    private String userAvatarUrl;

    private LocalDateTime createdAt;
    private Long reactionCount;

    private Map<String, Long> reactionSummary;
    private String myReaction;
    private Boolean isPinned;
    private Long parentId;
}
