package me.huynhducphu.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.common.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;
import java.util.Set;

/**
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 3:54 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.model.music
 */

@Entity
@Table(name = "artists")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SQLRestriction("is_deleted = false")
public class Artist extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Tên nghệ sĩ
    @Column(columnDefinition = "VARCHAR(150)", nullable = false)
    private String name;

    // Tiểu sử nghệ sĩ
    @Column(columnDefinition = "TEXT")
    private String bio;

    // Ảnh đại diện nghệ sĩ (lấy từ 3S xuống)
    @Column(nullable = false)
    private String imgUrl;

    // Các bài hát mà nghệ sĩ này tham gia với vai trò khác nhau
    @OneToMany(mappedBy = "artist")
    @ToString.Exclude
    private List<SongArtistRole> songRoles;

    // Các album mà nghệ sĩ này sở hữu/tạo ra
    @OneToMany(mappedBy = "albumOwner")
    @ToString.Exclude
    private List<Album> ownAlbums;

    // Các album mà nghệ sĩ này được giới thiệu (hoặc được tham gia trong đó)
    @ManyToMany(mappedBy = "featuredArtists")
    @ToString.Exclude
    private Set<Album> albums;

    @Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDeleted = false;
}
