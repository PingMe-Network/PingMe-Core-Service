package me.huynhducphu.ping_me.model.chat;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.common.BaseEntity;
import me.huynhducphu.ping_me.model.constant.RoomType;

import java.time.LocalDateTime;

/**
 * Admin 8/10/2025
 **/
@Entity
@Table(name = "rooms")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "room_type", nullable = false)
    @Enumerated(EnumType.STRING)
    // DIRECT (1-1), GROUP (n-n)
    private RoomType roomType;


    @Column(name = "direct_key", unique = true)
    // ROOM KEY (Nếu chat n-n thì null)
    private String directKey;

    // ROOM NAME (nếu chat 1-1 thì NULL)
    private String name;

    // ROOM IMAGE URL (nếu chat 1-1 thì NULL)
    private String roomImgUrl;

    @Column(name = "theme", columnDefinition = "varchar(50) default 'DEFAULT'")
    private String theme;

    @Column(name = "last_message_id")
    private String lastMessageId;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
}
