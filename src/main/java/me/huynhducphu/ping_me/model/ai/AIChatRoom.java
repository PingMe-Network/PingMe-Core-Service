package me.huynhducphu.ping_me.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AIChatRoom implements Persistable<UUID> {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Indexed
    @Field("user_id")
    private Long userId;

    @Indexed
    @Field("title")
    private String title;

    @Field("total_msg_count")
    private int totalMsgCount = 0;

    @Field("latest_summary")
    private String latestSummary; // Ký ức dài hạn tích lũy từ GPT-5 Nano

    @Field("msg_count_since_last_summary")
    private int interactCountSinceLastSummary = 0; // Để biết khi nào đạt ngưỡng 10-20 tin để tóm tắt tiếp

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate // Dùng đúng annotation cho ngày cập nhật
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
