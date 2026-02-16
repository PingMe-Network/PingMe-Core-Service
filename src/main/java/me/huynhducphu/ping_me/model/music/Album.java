package me.huynhducphu.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.common.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 3:41 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.model.music
 */

@Entity
@Table(name = "albums")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SQLRestriction("is_deleted = false")
public class Album extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    //Tên album
    @Column(columnDefinition = "VARCHAR(150)", nullable = false)
    private String title;

    //Chủ sở hữu album (tác giả tạo ra album này)
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Artist albumOwner;

    //Ảnh bìa album (lấy từ S3 xuống)
    @Column(nullable = false)
    private String coverImageUrl;

    //Danh sách thể loại của album
    @ManyToMany
    @JoinTable(
            name = "album_genre",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @ToString.Exclude
    private Set<Genre> genres = new HashSet<>();

    //Danh sách bài hát trong album
    @ManyToMany
    @JoinTable(
            name = "album_song",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    @ToString.Exclude
    private Set<Song> songs = new HashSet<>();

    //Danh sách nghệ sĩ tham gia trong album
    @ManyToMany
    @JoinTable(
            name = "album_artist",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    @ToString.Exclude
    private Set<Artist> featuredArtists = new HashSet<>();

    //Số lần album được phát
    @Column(nullable = false)
    private Long playCount = 0L;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "is_deleted")
    private boolean isDeleted = false;
}
