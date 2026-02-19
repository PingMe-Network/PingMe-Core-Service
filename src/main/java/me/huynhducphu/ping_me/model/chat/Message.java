package me.huynhducphu.ping_me.model.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;
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
                name = "idx_msg_room_id_id",
                def = "{'roomId': 1, '_id': -1}"
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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message {

    @Id
    @EqualsAndHashCode.Include
    String id;

    /**
     * =====================================
     * Nội dung chính
     * =====================================
     */

    @Field("content")
    String content;

    @Field("type")
    MessageType type;

    @Field("client_msg_id")
    UUID clientMsgId;

    @Field("is_active")
    Boolean isActive = true;

    @CreatedDate
    @Field("created_at")
    LocalDateTime createdAt;

    /**
     * =====================================
     * Quan hệ phụ thuộc
     * =====================================
     */

    @Field("sender_id")
    Long senderId;

    @Field("room_id")
    Long roomId;

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }
}
