package org.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.ping_me.model.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * Entity bảng trung gian Playlist-Song
 * Lưu thứ tự bài hát trong playlist
 */
@Entity
@Table(name = "playlist_songs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"playlist_id", "song_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaylistSong extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * =====================================
     * Quan hệ chính (Playlist ↔ Song)
     * =====================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    Song song;

    /**
     * =====================================
     * Vị trí & Thời gian thêm
     * =====================================
     */

    @Column(name = "position_index", nullable = false)
    Integer position;

    @Column(name = "added_at")
    LocalDateTime addedAt = LocalDateTime.now();
}
