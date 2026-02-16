package me.huynhducphu.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.common.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;
import java.util.Set;

/**
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 3:39 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.model.music
 */

@Entity
@Table(name = "songs")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SQLRestriction("is_deleted = false")
public class Song extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    //Tiêu đề bài hát
    @Column(columnDefinition = "VARCHAR(150)", nullable = false)
    private String title;

    //Thời lượng bài hát, tính bằng giây
    @Column(nullable = false)
    private int duration;

    //URL của bài hát (lấy từ S3 xuống)
    @Column(nullable = false)
    private String songUrl;

    //URL hình ảnh đại diện của bài hát (lấy từ S3 xuống)
    @Column(nullable = false)
    private String imgUrl;

    //Danh sách các nghệ sĩ và vai trò của họ trong bài hát
    //Bởi vì 1 bài hát có thể có nhiều nghệ sĩ với các vai trò khác nhau (ca sĩ chính, ca sĩ phụ, nhạc sĩ, v.v.)
    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<SongArtistRole> artistRoles;

    //Danh sách các album chứa bài hát này
    //Một bài hat có thể xuất hiện trong nhiều album khác nhau (album gốc, album tuyển tập, v.v.)
    //Và 1 album có thể chứa nhiều bài hát
    @ManyToMany(mappedBy = "songs")
    @ToString.Exclude
    private Set<Album> albums;


    //Danh sách các thể loại của bài hát
    //Một bài hát có thể thuộc nhiều thể loại khác nhau (pop, rock, jazz, v.v.)
    //Và một thể loại có thể bao gồm nhiều bài hát
    @ManyToMany
    @JoinTable(
            name = "song_genre",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @ToString.Exclude
    private Set<Genre> genres;

    //Số lần bài hát được phát
    @Column(nullable = false)
    private Long playCount = 0L;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "is_deleted")
    private boolean isDeleted = false;
}
