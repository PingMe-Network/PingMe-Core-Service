package me.huynhducphu.ping_me.service.music;

import me.huynhducphu.ping_me.dto.request.music.GenreRequest;
import me.huynhducphu.ping_me.dto.response.music.GenreResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Le Tran Gia Huy
 * @created 22/11/2025 - 9:13 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.service.music
 */
public interface GenreService {
    GenreResponse getGenreById(Long id);

    Page<GenreResponse> getAllGenres(Pageable pageable);

    GenreResponse createGenre(GenreRequest request);

    GenreResponse updateGenre(Long id, GenreRequest request);

    void softDeleteGenre(Long id);

    void restoreGenre(Long id);

    void hardDeleteGenre(Long id);
}
