package org.ping_me.dto.response.chat.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ping_me.model.constant.ReminderStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReminderResponse {
    private Long id;
    private String messageId;
    private Long roomId;
    private Long createdByUserId;
    private String title;
    private String body;
    private LocalDateTime remindAt;
    private String timezone;
    private String repeatRule;
    private ReminderStatus status;
    private LocalDateTime triggeredAt;
    private LocalDateTime completedAt;
    private LocalDateTime canceledAt;
}
