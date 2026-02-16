package me.huynhducphu.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.common.BaseEntity;
import me.huynhducphu.ping_me.model.constant.ArtistRole;

/**
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 4:06 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.model.music
 */

@Entity
@Table(name = "song_artist_role")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SongArtistRole extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    //Bên dưới là bài hát và nghệ sĩ liên kết với vai trò cụ thể
    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Enumerated(EnumType.STRING)
    private ArtistRole role;
}
