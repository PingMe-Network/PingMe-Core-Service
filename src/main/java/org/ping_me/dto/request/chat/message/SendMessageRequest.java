package org.ping_me.dto.request.chat.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.ping_me.model.constant.MessageType;

/**
 * Admin 8/26/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SendMessageRequest {
    @NotBlank(message = "Nội dung không được để trống")
    @Length(max = 1000, message = "Nội dung không được vượt quá 1000 ký tự")
    private String content;

    @NotBlank(message = "UUID không được để trống")
    private String clientMsgId;

    @Schema(allowableValues = {"TEXT", "IMAGE", "VIDEO", "FILE"})
    private MessageType type;

    @NotNull(message = "Mã phòng không được để trống")
    private Long roomId;
}
