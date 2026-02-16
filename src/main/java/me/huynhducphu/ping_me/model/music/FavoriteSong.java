package me.huynhducphu.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorite_songs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "song_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FavoriteSong extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
