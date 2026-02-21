package org.ping_me.repository.jpa.music;

import org.ping_me.model.music.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 6:22 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.repository
 */

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    // 1. Kiểm tra trùng tên (khi tạo mới/update)
    boolean existsByNameIgnoreCase(String name);

    // 2. Tìm kiếm (Soft Delete)
    @Query(value = "SELECT * FROM genres WHERE id = :id AND is_deleted = true", nativeQuery = true)
    Optional<Genre> findSoftDeletedGenre(@Param("id") Long id);

    // 3. Tìm kiếm bất chấp trạng thái (Hard Delete)
    @Query(value = "SELECT * FROM genres WHERE id = :id", nativeQuery = true)
    Optional<Genre> findByIdIgnoringDeleted(@Param("id") Long id);

    // 4. Kiểm tra ràng buộc trước khi xóa
    // Đếm xem có bao nhiêu bài hát đang dùng genre này
    @Query("SELECT COUNT(s) FROM Song s JOIN s.genres g WHERE g.id = :genreId")
    long countSongsByGenreId(@Param("genreId") Long genreId);

    // Đếm xem có bao nhiêu album đang dùng genre này
    @Query("SELECT COUNT(a) FROM Album a JOIN a.genres g WHERE g.id = :genreId")
    long countAlbumsByGenreId(@Param("genreId") Long genreId);
}
