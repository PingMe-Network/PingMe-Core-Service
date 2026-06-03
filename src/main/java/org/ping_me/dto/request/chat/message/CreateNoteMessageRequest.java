package org.ping_me.dto.request.chat.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateNoteMessageRequest {

    @NotBlank(message = "Tiêu đề ghi chú không được để trống")
    @Length(max = 200, message = "Tiêu đề ghi chú không được vượt quá 200 ký tự")
    private String title;

    @NotBlank(message = "Nội dung ghi chú không được để trống")
    @Length(max = 2000, message = "Nội dung ghi chú không được vượt quá 2000 ký tự")
    private String body;

    private Boolean pinToTop = false;

    private String repliedMessageId;

    @NotBlank(message = "UUID không được để trống")
    private String clientMsgId;

    @NotNull(message = "Mã phòng không được để trống")
    private Long roomId;
}
