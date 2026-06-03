package org.ping_me.model.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ping_me.model.constant.ReminderStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_reminders",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_reminder_message", columnNames = "message_id")
        },
        indexes = {
                @Index(name = "idx_chat_reminder_room", columnList = "room_id"),
                @Index(name = "idx_chat_reminder_due", columnList = "status, remind_at"),
                @Index(name = "idx_chat_reminder_created_by", columnList = "created_by_user_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatReminder extends ChatRoomEntry {

    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @Column(nullable = false, length = 64)
    private String timezone;

    @Column(name = "repeat_rule", nullable = false, length = 32)
    private String repeatRule = "NONE";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;
}
