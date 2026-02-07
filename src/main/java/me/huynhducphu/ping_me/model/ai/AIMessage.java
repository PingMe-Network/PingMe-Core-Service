package me.huynhducphu.ping_me.model.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.huynhducphu.ping_me.model.constant.AIMessageType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author Le Tran Gia Huy
 * @created 27/01/2026 - 8:00 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.model.ai
 */

@Document(collection = "ai-messages")
@CompoundIndexes({
        @CompoundIndex(
                name = "idx_msg_room_created_id",
                def = "{'chatRoomId': 1, 'created_at': -1, '_id': -1}"
        )
})
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AIMessage {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Field("chatRoomId")
    private UUID chatRoomId;

    @Indexed
    @Field("user_id")
    private Long userId;

    @Field("sender")
    private AIMessageType type; // "sent" or "received"

    @Field("content")
    private String content;

    @Field("attachments")
    private List<Attachment> attachments;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @Data
    @AllArgsConstructor
    public static class Attachment {
        private String url;
        private String fileType; // "image/png", "application/pdf", v.v.
    }
}
