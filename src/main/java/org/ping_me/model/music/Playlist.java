package org.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.ping_me.model.User;
import org.ping_me.model.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity đại diện cho Playlist do User tạo
 */
@Entity
@Table(name = "playlists")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Playlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * =====================================
     * Thông tin cơ bản (Tên, Mô tả, Ảnh bìa)
     * =====================================
     */

    @Column(nullable = false)
    String name;

    @Column(name = "description", length = 1000)
    String description;

    @Column(name = "cover_img")
    String coverImg;

    /**
     * =====================================
     * Cấu hình & Thời gian
     * =====================================
     */

    @Column(name = "is_public", nullable = false)
    Boolean isPublic = true;

    @Column(name = "created_at")
    LocalDateTime createdAt = LocalDateTime.now();

    /**
     * =====================================
     * Chủ sở hữu playlist
     * =====================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    /**
     * =====================================
     * Danh sách bài hát trong playlist
     * =====================================
     */

    // convenience bi-directional mapping (optional)
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<PlaylistSong> playlistSongs = new HashSet<>();
}
