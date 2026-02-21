package org.ping_me.dto.request.reels;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpsertReelCommentRequest {
    @NotBlank(message = "Nội dung bình luận không được trống")
    @Size(max = 500, message = "Bình luận không quá 500 ký tự")
    private String content;
    private Long parentId;
}
