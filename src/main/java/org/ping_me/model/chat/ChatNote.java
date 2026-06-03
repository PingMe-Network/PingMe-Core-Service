package org.ping_me.model.chat;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "chat_notes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_note_message", columnNames = "message_id")
        },
        indexes = {
                @Index(name = "idx_chat_note_room", columnList = "room_id"),
                @Index(name = "idx_chat_note_created_by", columnList = "created_by_user_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatNote extends ChatRoomEntry {
}
