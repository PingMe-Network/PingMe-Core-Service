package me.huynhducphu.ping_me.service.music.impl;

import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.request.music.GenreRequest;
import me.huynhducphu.ping_me.dto.response.music.GenreResponse;
import me.huynhducphu.ping_me.dto.response.music.misc.GenreDto;
import me.huynhducphu.ping_me.model.music.Genre;
import me.huynhducphu.ping_me.repository.jpa.music.GenreRepository;
import me.huynhducphu.ping_me.service.music.GenreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

/**
 * @author Le Tran Gia Huy
 * @created 22/11/2025 - 2:05 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.service.music.impl
 */

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    @Override
    public GenreResponse getGenreById(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại với ID: " + id));
        return mapToResponse(genre);
    }

    @Override
    public Page<GenreResponse> getAllGenres(Pageable pageable) {
        Page<Genre> genres = genreRepository.findAll(pageable);
        return genres.map(this::mapToResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GenreResponse createGenre(GenreRequest request) {
        // 1. Check trùng tên
        if (genreRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Thể loại '" + request.getName() + "' đã tồn tại");
        }

        Genre genre = new Genre();
        genre.setName(request.getName());

        // Khởi tạo list rỗng để tránh null pointer
        genre.setSongs(new HashSet<>());
        genre.setAlbums(new HashSet<>());

        // isDeleted mặc định false (do entity config)

        Genre savedGenre = genreRepository.save(genre);
        return mapToResponse(savedGenre);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GenreResponse updateGenre(Long id, GenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));

        // 1. Check trùng tên (nếu tên thay đổi)
        if (!genre.getName().equalsIgnoreCase(request.getName())) {
            if (genreRepository.existsByNameIgnoreCase(request.getName())) {
                throw new RuntimeException("Thể loại '" + request.getName() + "' đã tồn tại");
            }
        }

        genre.setName(request.getName());

        Genre updatedGenre = genreRepository.save(genre);
        return mapToResponse(updatedGenre);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDeleteGenre(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));

        // 2. CHECK RÀNG BUỘC (QUAN TRỌNG)
        // Không nên xóa thể loại đang được sử dụng, vì sẽ làm Song/Album bị mất thông tin phân loại
        long songCount = genreRepository.countSongsByGenreId(id);
        long albumCount = genreRepository.countAlbumsByGenreId(id);

        if (songCount > 0 || albumCount > 0) {
            throw new RuntimeException(String.format(
                    "Không thể xóa thể loại này. Nó đang được dùng bởi %d bài hát và %d album.",
                    songCount, albumCount));
        }

        genre.setDeleted(true);
        genreRepository.save(genre);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreGenre(Long id) {
        Genre genre = genreRepository.findSoftDeletedGenre(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại trong thùng rác"));

        genre.setDeleted(false);
        genreRepository.save(genre);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteGenre(Long id) {
        Genre genre = genreRepository.findByIdIgnoringDeleted(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));

        // Check ràng buộc lần cuối
        long songCount = genreRepository.countSongsByGenreId(id);
        long albumCount = genreRepository.countAlbumsByGenreId(id);
        if (songCount > 0 || albumCount > 0) {
            throw new RuntimeException("Vẫn còn dữ liệu liên quan, không thể xóa vĩnh viễn!");
        }

        genreRepository.delete(genre);
    }

    // --- Helper ---
    private GenreResponse mapToResponse(Genre genre) {
        return new GenreResponse(genre.getId(), genre.getName());
    }

    private GenreDto mapToDto(Genre genre) {
        return new GenreDto(
                genre.getId(),
                genre.getName()
        );
    }
}
