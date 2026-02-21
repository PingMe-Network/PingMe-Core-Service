package org.ping_me.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;
import org.ping_me.model.constant.AIMessageType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
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
        ),
        @CompoundIndex(
                name = "idx_msg_room_user_created",
                def = "{'chatRoomId': 1, 'user_id': 1, 'created_at': -1}"
        ),
        @CompoundIndex(
                name = "idx_msg_user_created",
                def = "{'user_id': 1, 'created_at': -1}"
        )
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AIMessage implements Persistable<@NonNull UUID> {

    @Id
    UUID id;

    /**
     * =====================================
     * Nội dung chính
     * =====================================
     */

    @Field("content")
    String content;

    @Field("attachments")
    List<Attachment> attachments;

    @Field("sender")
    AIMessageType type;

    @CreatedDate
    @Field("created_at")
    LocalDateTime createdAt;

    /**
     * =====================================
     * Quan hệ phụ thuộc
     * =====================================
     */

    @Field("chatRoomId")
    UUID chatRoomId;

    @Indexed
    @Field("user_id")
    Long userId;


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Attachment {
        String url;
        String fileType;
    }

    @JsonIgnore
    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
