package org.ping_me.dto.request.chat.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateReminderMessageRequest {

    @NotBlank(message = "Tiêu đề nhắc hẹn không được để trống")
    @Length(max = 200, message = "Tiêu đề nhắc hẹn không được vượt quá 200 ký tự")
    private String title;

    @NotBlank(message = "Nội dung nhắc hẹn không được để trống")
    @Length(max = 2000, message = "Nội dung nhắc hẹn không được vượt quá 2000 ký tự")
    private String body;

    @NotNull(message = "Thời gian nhắc hẹn không được để trống")
    private LocalDateTime remindAt;

    @NotBlank(message = "Múi giờ không được để trống")
    @Length(max = 64, message = "Múi giờ không được vượt quá 64 ký tự")
    private String timezone;

    @Length(max = 32, message = "Kiểu lặp lại không được vượt quá 32 ký tự")
    private String repeatRule = "NONE";

    private Boolean pinToTop = false;

    private String repliedMessageId;

    @NotBlank(message = "UUID không được để trống")
    private String clientMsgId;

    @NotNull(message = "Mã phòng không được để trống")
    private Long roomId;
}
