package org.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.ping_me.model.User;
import org.ping_me.model.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * Entity bảng trung gian User-Song: Bài hát yêu thích
 * Mỗi record = 1 user thích 1 bài hát
 */
@Entity
@Table(name = "favorite_songs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "song_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level =  AccessLevel.PRIVATE)
public class FavoriteSong extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * =====================================
     * Quan hệ chính (User ↔ Song)
     * =====================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    Song song;

    /**
     * =====================================
     * Thời gian thêm vào danh sách yêu thích
     * =====================================
     */

    @Column(name = "created_at")
    LocalDateTime createdAt = LocalDateTime.now();
}
