package me.huynhducphu.ping_me.repository.jpa.music;

import me.huynhducphu.ping_me.model.music.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 6:21 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.repository
 */

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Page<Artist> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT COUNT(a) > 0 FROM Album a WHERE a.albumOwner.id = :artistId")
    boolean hasOwnedAlbums(@Param("artistId") Long artistId);

    @Query("SELECT COUNT(r) > 0 FROM SongArtistRole r WHERE r.artist.id = :artistId")
    boolean hasSongRoles(@Param("artistId") Long artistId);

    // --- THÊM MỚI ---

    // Tìm nghệ sĩ đang nằm trong thùng rác (để Restore)
    @Query(value = "SELECT * FROM artists WHERE id = :id AND is_deleted = true", nativeQuery = true)
    java.util.Optional<Artist> findSoftDeletedArtist(@Param("id") Long id);

    // Tìm nghệ sĩ bất kể trạng thái (để Hard Delete)
    @Query(value = "SELECT * FROM artists WHERE id = :id", nativeQuery = true)
    java.util.Optional<Artist> findByIdIgnoringDeleted(@Param("id") Long id);
}
