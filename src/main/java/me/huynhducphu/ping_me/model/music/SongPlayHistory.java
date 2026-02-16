package me.huynhducphu.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "song_play_history")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SongPlayHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ai nghe bài hát
    @Column(nullable = false)
    private Long userId;

    // Bài hát nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    // Thời điểm nghe
    @Column(nullable = false)
    private LocalDateTime playedAt;
}
