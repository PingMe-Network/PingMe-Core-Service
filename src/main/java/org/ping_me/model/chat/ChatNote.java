package org.ping_me.model.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ping_me.model.User;
import org.ping_me.model.common.BaseEntity;

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
public class ChatNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "message_id", nullable = false, length = 64)
    private String messageId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String body;
}
