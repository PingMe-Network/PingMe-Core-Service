package org.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;
import org.ping_me.model.common.BaseEntity;

import java.util.Set;

/**
 * Entity đại diện cho Thể loại nhạc (Pop, Rock, Jazz,...)
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 3:48 PM
 */
@Entity
@Table(name = "genres")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SQLRestriction("is_deleted = false")
@FieldDefaults(level =  AccessLevel.PRIVATE)
public class Genre extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * =====================================
     * Tên thể loại
     * =====================================
     */

    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    String name;

    /**
     * =====================================
     * Trạng thái
     * =====================================
     */

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE", name = "is_deleted")
    boolean isDeleted = false;

    /**
     * =====================================
     * Quan hệ với Song & Album
     * =====================================
     */

    //Danh sách bài hát thuộc thể loại này
    @ManyToMany(mappedBy = "genres")
    @ToString.Exclude
    Set<Song> songs;

    //Danh sách album thuộc thể loại này
    @ManyToMany(mappedBy = "genres")
    @ToString.Exclude
    Set<Album> albums;
}
