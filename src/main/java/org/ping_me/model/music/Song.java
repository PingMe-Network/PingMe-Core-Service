package org.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;
import org.ping_me.model.common.BaseEntity;

import java.util.List;
import java.util.Set;

/**
 * Entity đại diện cho Bài hát
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 3:39 PM
 */
@Entity
@Table(name = "songs")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SQLRestriction("is_deleted = false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Song extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * =====================================
     * Nội dung chính (Tiêu đề, Thời lượng, URL)
     * =====================================
     */

    @Column(columnDefinition = "VARCHAR(150)", nullable = false)
    String title;

    @Column(nullable = false)
    int duration;

    @Column(nullable = false)
    String songUrl;

    @Column(nullable = false)
    String imgUrl;

    /**
     * =====================================
     * Trạng thái & Số liệu
     * =====================================
     */

    @Column(nullable = false)
    Long playCount = 0L;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "is_deleted")
    boolean isDeleted = false;

    /**
     * =====================================
     * Quan hệ với nghệ sĩ (Ca sĩ, nhạc sĩ,...)
     * =====================================
     */

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
    @ToString.Exclude
    List<SongArtistRole> artistRoles;

    /**
     * =====================================
     * Quan hệ với Album & Thể loại
     * =====================================
     */

    //Danh sách các album chứa bài hát này
    //Một bài hat có thể xuất hiện trong nhiều album khác nhau (album gốc, album tuyển tập, v.v.)
    //Và 1 album có thể chứa nhiều bài hát
    @ManyToMany(mappedBy = "songs")
    @ToString.Exclude
    Set<Album> albums;


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
    Set<Genre> genres;
}
