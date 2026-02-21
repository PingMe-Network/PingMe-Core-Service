package org.ping_me.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Le Tran Gia Huy
 * @created 27/01/2026 - 8:00 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.model.ai
 */

@Document(collection = "ai-chat-rooms")
@CompoundIndex(name = "idx_user_updated", def = "{'user_id': 1, 'updated_at': -1}")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AIChatRoom implements Persistable<@NonNull UUID> {

    @Id
    UUID id;

    /**
     * =====================================
     * Nội dung chính
     * =====================================
     */

    @Indexed
    @Field("title")
    String title;

    @Field("total_msg_count")
    int totalMsgCount = 0;

    @Field("latest_summary")
    String latestSummary; // Ký ức dài hạn tích lũy từ GPT-5 Nano

    @Field("msg_count_since_last_summary")
    int interactCountSinceLastSummary = 0; // Để biết khi nào đạt ngưỡng 10-20 tin để tóm tắt tiếp

    @CreatedDate
    @Field("created_at")
    LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    LocalDateTime updatedAt;

    /**
     * =====================================
     * Quan hệ phụ thuộc
     * =====================================
     */

    @Indexed
    @Field("user_id")
    Long userId;

    @JsonIgnore
    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
