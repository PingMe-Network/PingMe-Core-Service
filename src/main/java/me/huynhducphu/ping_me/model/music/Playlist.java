package me.huynhducphu.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "playlists")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Playlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // owner

    @Column(nullable = false)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "cover_img")
    private String coverImg;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // convenience bi-directional mapping (optional)
    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlaylistSong> playlistSongs = new HashSet<>();
}
