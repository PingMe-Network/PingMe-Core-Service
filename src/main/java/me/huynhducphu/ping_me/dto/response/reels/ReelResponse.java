package me.huynhducphu.ping_me.dto.response.reels;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReelResponse {
    private Long id;
    private String videoUrl;
    private String caption;

    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Boolean isLikedByMe;
    private Boolean isSavedByMe;

    private Long userId;
    private String userName;
    private String userAvatarUrl;
    private List<String> hashtags;

    private LocalDateTime createdAt;

}
