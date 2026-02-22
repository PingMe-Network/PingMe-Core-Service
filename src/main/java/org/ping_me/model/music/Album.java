package org.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;
import org.ping_me.model.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity đại diện cho Album nhạc
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 3:41 PM
 */
@Entity
@Table(name = "albums")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SQLRestriction("is_deleted = false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Album extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * =====================================
     * Nội dung chính
     * =====================================
     */

    @Column(columnDefinition = "VARCHAR(150)", nullable = false)
    String title;

    @Column(nullable = false)
    String coverImageUrl;

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
     * Chủ sở hữu
     * =====================================
     */

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    Artist albumOwner;

    /**
     * =====================================
     * Quan hệ phụ thuộc (Thể loại, Bài hát, Nghệ sĩ tham gia)
     * =====================================
     */

    @ManyToMany
    @JoinTable(
            name = "album_genre",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @ToString.Exclude
    Set<Genre> genres = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "album_song",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    @ToString.Exclude
    Set<Song> songs = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "album_artist",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    @ToString.Exclude
    Set<Artist> featuredArtists = new HashSet<>();
}
