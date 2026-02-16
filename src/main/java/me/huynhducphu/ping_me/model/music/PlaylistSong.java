package me.huynhducphu.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_songs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"playlist_id", "song_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlaylistSong extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    // position for ordering in playlist (0-based or 1-based - choose consistent in frontend)
    @Column(name = "position_index", nullable = false)
    private Integer position;

    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();
}
