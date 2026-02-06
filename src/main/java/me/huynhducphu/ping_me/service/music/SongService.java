package me.huynhducphu.ping_me.service.music;

import me.huynhducphu.ping_me.dto.request.music.SongRequest;
import me.huynhducphu.ping_me.dto.response.music.SongResponse;
import me.huynhducphu.ping_me.dto.response.music.SongResponseWithAllAlbum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SongService {
    Long MAX_AUDIO_SIZE = 20L * 1024L * 1024L;
    Long MAX_COVER_SIZE = 5L * 1024L * 1024L;

    Page<SongResponseWithAllAlbum> getAllSongs(Pageable pageable);

    SongResponse getSongById(Long id);

    Page<SongResponseWithAllAlbum> getSongByGenre(Long id, Pageable pageable);


    Page<SongResponseWithAllAlbum> getSongByAlbum(Long id, Pageable pageable);

    Page<SongResponseWithAllAlbum> getSongsByArtist(Long artistId, Pageable pageable);

    List<SongResponseWithAllAlbum> getTopPlayedSongs(int limit);

    Page<SongResponse> getSongByTitle(String title, Pageable pageable);

    List<SongResponse> save(
            SongRequest dto,
            MultipartFile musicFile,
            MultipartFile imgFile
    );

    // Trả về List vì 1 bài hát có thể thuộc nhiều album -> flatten ra nhiều dòng
    List<SongResponse> update(
            Long id,
            SongRequest dto,
            MultipartFile musicFile,
            MultipartFile imgFile
    ) throws IOException;

    void hardDelete(Long id);

    void softDelete(Long id);

    void restore(Long id);

    @Transactional
    void increasePlayCount(Long songId);
}
