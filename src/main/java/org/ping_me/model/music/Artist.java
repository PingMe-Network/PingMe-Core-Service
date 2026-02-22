package org.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;
import org.ping_me.model.common.BaseEntity;

import java.util.List;
import java.util.Set;

/**
 * Entity đại diện cho Nghệ sĩ/Ca sĩ
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 3:54 PM
 */
@Entity
@Table(name = "artists")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SQLRestriction("is_deleted = false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Artist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * =====================================
     * Thông tin cơ bản (Tên, Tiểu sử, Ảnh)
     * =====================================
     */

    @Column(columnDefinition = "VARCHAR(150)", nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String bio;

    @Column(nullable = false)
    String imgUrl;

    /**
     * =====================================
     * Trạng thái
     * =====================================
     */

    @Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    boolean isDeleted = false;

    /**
     * =====================================
     * Quan hệ với bài hát (Vai trò trong bài hát)
     * =====================================
     */

    @OneToMany(mappedBy = "artist")
    @ToString.Exclude
    List<SongArtistRole> songRoles;

    /**
     * =====================================
     * Quan hệ với album (Sở hữu, Tham gia)
     * =====================================
     */

    @OneToMany(mappedBy = "albumOwner")
    @ToString.Exclude
    List<Album> ownAlbums;

    @ManyToMany(mappedBy = "featuredArtists")
    @ToString.Exclude
    Set<Album> albums;
}
