package org.ping_me.model.music;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.ping_me.model.common.BaseEntity;
import org.ping_me.model.constant.ArtistRole;

/**
 * Entity bảng trung gian Song-Artist
 * Lưu vai trò của nghệ sĩ trong bài hát (Ca sĩ chính, phối khí,...)
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 4:06 PM
 */
@Entity
@Table(name = "song_artist_role")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level =  AccessLevel.PRIVATE)
public class SongArtistRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * =====================================
     * Quan hệ chính (Song ↔ Artist)
     * =====================================
     */

    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    Song song;

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    Artist artist;

    /**
     * =====================================
     * Vai trò trong bài hát
     * =====================================
     */

    @Enumerated(EnumType.STRING)
    ArtistRole role;
}
