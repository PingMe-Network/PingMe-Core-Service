package me.huynhducphu.ping_me.model.chat;

import lombok.*;
import me.huynhducphu.ping_me.model.constant.MessageType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Admin 8/10/2025
 **/
@Document(collection = "messages")
@CompoundIndexes({
        @CompoundIndex(
                name = "uq_msg_idem",
                def = "{'roomId': 1, 'senderId': 1, 'clientMsgId': 1}",
                unique = true
        ),
        @CompoundIndex(
                name = "idx_msg_room_created_id",
                def = "{'roomId': 1, 'createdAt': -1, '_id': -1}"
        )
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @Field("content")
    private String content;

    @Field("type")
    private MessageType type;

    @Field("client_msg_id")
    private UUID clientMsgId;

    @Indexed
    @Field("sender_id")
    private Long senderId;

    @Indexed
    @Field("room_id")
    private Long roomId;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("is_active")
    private Boolean isActive = true;

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }
}
