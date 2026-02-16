package me.huynhducphu.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.common.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.util.Set;

/**
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 3:48 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.model.music
 */

@Entity
@Table(name = "genres")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SQLRestriction("is_deleted = false")
public class Genre extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    //Tên thể loại
    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    private String name;

    //Danh sách bài hát thuộc thể loại này
    @ManyToMany(mappedBy = "genres")
    @ToString.Exclude
    private Set<Song> songs;

    //Danh sách album thuộc thể loại này
    @ManyToMany(mappedBy = "genres")
    @ToString.Exclude
    private Set<Album> albums;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "is_deleted")
    private boolean isDeleted = false;
}
