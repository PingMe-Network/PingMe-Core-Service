package me.huynhducphu.ping_me.service.music.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.dto.request.music.SongRequest;
import me.huynhducphu.ping_me.dto.request.music.misc.SongArtistRequest;
import me.huynhducphu.ping_me.dto.response.music.SongResponse;
import me.huynhducphu.ping_me.dto.response.music.SongResponseWithAllAlbum;
import me.huynhducphu.ping_me.dto.response.music.misc.AlbumSummaryDto;
import me.huynhducphu.ping_me.dto.response.music.misc.ArtistSummaryDto;
import me.huynhducphu.ping_me.dto.response.music.misc.GenreDto;
import me.huynhducphu.ping_me.model.constant.ArtistRole;
import me.huynhducphu.ping_me.model.music.*;
import me.huynhducphu.ping_me.repository.jpa.music.*;
import me.huynhducphu.ping_me.service.ffmpeg.constants.MediaType;
import me.huynhducphu.ping_me.service.music.SongService;
import me.huynhducphu.ping_me.service.music.util.AudioUtil;
import me.huynhducphu.ping_me.service.s3.S3Service;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final SongArtistRoleRepository songArtistRoleRepository;
    private final AudioUtil audioUtil;
    private final SongPlayHistoryRepository songPlayHistoryRepository;

    @Autowired
    @Qualifier("redisMessageStringTemplate")
    private RedisTemplate<String, String> redis;
    private final S3Service s3Service;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public Page<SongResponseWithAllAlbum> getAllSongs(Pageable pageable) {
        Page<Song> songPage = songRepository.findAll(pageable);
        return songPage.map(this::mapToSongResponseWithAllAlbums);
    }


    @Override
    public SongResponse getSongById(Long id) {
        Song song = songRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài hát với id: " + id));

        return mapToSongResponse(song, song.getAlbums() != null && !song.getAlbums().isEmpty()
                ? song.getAlbums().iterator().next()
                : null);
    }

    @Override
    public Page<SongResponse> getSongByTitle(String title, Pageable pageable) {
        Page<Song> songs = songRepository.findSongsByTitleContainingIgnoreCase(title, pageable);

        List<SongResponse> content = flattenSongsWithAlbums(songs.getContent());
        return new PageImpl<>(content, pageable, songs.getTotalElements());
    }


    @Override
    public Page<SongResponseWithAllAlbum> getSongByGenre(Long id, Pageable pageable) {
        if (id == null) {
            throw new RuntimeException("Genre ID không được trống");
        }

        Page<Song> songs = songRepository.findSongsByGenreId(id, pageable);
        return songs.map(this::mapToSongResponseWithAllAlbums);
    }


    @Override
    public Page<SongResponseWithAllAlbum> getSongByAlbum(Long id, Pageable pageable) {
        if (id == null) {
            throw new RuntimeException("Album ID không được trống");
        }

        Page<Song> songs = songRepository.findSongsByAlbumId(id, pageable);
        return songs.map(this::mapToSongResponseWithAllAlbums);
    }


    @Override
    public Page<SongResponseWithAllAlbum> getSongsByArtist(Long artistId, Pageable pageable) {
        if (artistId == null) {
            throw new RuntimeException("Artist ID không được trống");
        }

        Page<Song> songs = songRepository.findSongsByArtistId(artistId, pageable);
        return songs.map(this::mapToSongResponseWithAllAlbums);
    }


    // Cho phép truyền số lượng bài muốn lấy
    public List<SongResponseWithAllAlbum> getTopPlayedSongs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Song> topSongs = songRepository.findSongsByPlayCount(pageable);
        List<SongResponseWithAllAlbum> songResponsesWithAllAlbum = new ArrayList<>();
        topSongs.forEach(x -> {
            SongResponseWithAllAlbum songResponseWithAllAlbum = mapToSongResponseWithAllAlbums(x);
            songResponsesWithAllAlbum.add(songResponseWithAllAlbum);
        });
        return songResponsesWithAllAlbum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // Rollback nếu lỗi S3 hoặc DB
    public List<SongResponse> save(
            SongRequest dto, MultipartFile musicFile, MultipartFile imgFile
    ) {

        // 1. Validate file đầu vào (Bắt buộc phải có)
        if (musicFile == null || musicFile.isEmpty()) {
            throw new RuntimeException("Vui lòng tải lên file nhạc");
        }
        if (imgFile == null || imgFile.isEmpty()) {
            throw new RuntimeException("Vui lòng tải lên ảnh bìa");
        }

        // 2. Khởi tạo Song Entity
        var song = new Song();
        song.setTitle(dto.getTitle());

        song.setPlayCount(0L); // Mặc định 0 view

        // 3. Upload File Nhạc lên S3
        File compressedFile = null;
        try {
            int finalDuration = 0;
            try {
                finalDuration = audioUtil.getDurationFromMusicFile(musicFile);
            } catch (Exception e) {
                log.error("Backend không đọc được duration file: " + e.getMessage());
            }
            if (finalDuration <= 0 && dto.getDuration() > 0) {
                finalDuration = dto.getDuration();
            }
            if (finalDuration <= 0) {
                finalDuration = 0;
            }
            song.setDuration(finalDuration);
            // C. Tạo tên file mới (Luôn là .mp3 vì mình nén sang mp3)
            String audioFileName = UUID.randomUUID() + ".mp3";

            // E. Upload lên S3 (S3Service không biết đây là file fake, nó cứ upload thôi)
            String songUrl = s3Service.uploadCompressedFile(
                    musicFile,
                    "music/song",
                    audioFileName,
                    false,
                    MAX_AUDIO_SIZE,
                    MediaType.AUDIO
            );
            song.setSongUrl(songUrl);

        } finally {
            // F. QUAN TRỌNG: Dọn dẹp file nén tạm trên ổ cứng server
            // Dù upload thành công hay thất bại cũng phải xóa để tránh đầy ổ cứng
            if (compressedFile != null && compressedFile.exists()) {
                boolean deleted = compressedFile.delete();
                if (!deleted) System.err.println("Không xóa được file tạm: " + compressedFile.getAbsolutePath());
            }
        }

        // 4. Upload File Ảnh lên S3
        String imageFileName = generateFileName(imgFile);
        String imgUrl = s3Service.uploadFile(
                imgFile,
                "music/img", // Folder trên S3
                imageFileName,
                true,
                MAX_COVER_SIZE
        );
        song.setImgUrl(imgUrl);

        // 5. Xử lý Genre (Thể loại)
        if (dto.getGenreIds() != null && dto.getGenreIds().length > 0) {
            // Lưu ý: DTO tên là Names nhưng kiểu Long[] nên t hiểu là IDs
            var genreIds = Arrays.asList(dto.getGenreIds());
            var genres = new HashSet<>(genreRepository.findAllById(genreIds));
            song.setGenres(genres);
        }

        // 6. Lưu tạm Song để có ID (quan trọng cho bước ArtistRole)
        var savedSong = songRepository.save(song);

        // 7. Xử lý Artist (Main & Featured)
        List<SongArtistRole> artistRoles = new ArrayList<>();

        // 7a. Main Artist
        var mainArtist = artistRepository.findById(dto.getMainArtistId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nghệ sĩ chính"));

        var mainRole = new SongArtistRole();
        mainRole.setSong(savedSong);
        mainRole.setArtist(mainArtist);
        mainRole.setRole(ArtistRole.MAIN_ARTIST);
        artistRoles.add(mainRole);

        // 7b. Featured Artists
        if (dto.getOtherArtists() != null && !dto.getOtherArtists().isEmpty()) {
            // Dùng Set để check trùng lặp ID nghệ sĩ trong request (tránh 1 người add 2 lần)
            Set<Long> processedArtistIds = new HashSet<>();
            processedArtistIds.add(mainArtist.getId());

            for (SongArtistRequest artistReq : dto.getOtherArtists()) {
                Long artistId = artistReq.getArtistId();
                ArtistRole role = artistReq.getRole();

                // Validate: Không cho phép add lại Main Artist vào list phụ
                // Hoặc 1 người không thể xuất hiện 2 lần trong 1 bài hát (tùy nghiệp vụ)
                if (processedArtistIds.contains(artistId)) {
                    continue; // Skip nếu trùng
                }

                var artist = artistRepository.findById(artistId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nghệ sĩ ID: " + artistId));

                var artistRole = new SongArtistRole();
                artistRole.setSong(savedSong);
                artistRole.setArtist(artist);

                // QUAN TRỌNG: Lấy Role từ request thay vì fix cứng FEATURED_ARTIST
                artistRole.setRole(role);

                artistRoles.add(artistRole);
                processedArtistIds.add(artistId);
            }
        }

        // Lưu Batch roles
        songArtistRoleRepository.saveAll(artistRoles);
        savedSong.setArtistRoles(artistRoles); // Update lại object để map response

        // 8. Xử lý Album (CẬP NHẬT METADATA)
        if (dto.getAlbumIds() != null && dto.getAlbumIds().length > 0) {
            var albumIds = Arrays.asList(dto.getAlbumIds());
            var albums = new HashSet<>(albumRepository.findAllById(albumIds));

            // Lấy danh sách Featured Artists của bài hát hiện tại ra trước
            List<Artist> allArtistsInSong = artistRoles.stream()
                    .map(SongArtistRole::getArtist)
                    .toList();

            for (var album : albums) {
                // A. Thêm bài hát vào Album
                album.getSongs().add(savedSong);

                // B. Cập nhật Genre cho Album (Tự động merge, ko sợ trùng vì dùng Set)
                if (savedSong.getGenres() != null) {
                    album.getGenres().addAll(savedSong.getGenres());
                }

                // C. Cập nhật Featured Artist cho Album (Tự động merge)
                if (!allArtistsInSong.isEmpty()) {
                    // Lưu ý: Phải đảm bảo list featuredArtists trong Album đã được khởi tạo (new HashSet)
                    // Nếu chưa thì check null: if(album.getFeaturedArtists() == null) album.setFeaturedArtists(new HashSet<>());
                    album.getFeaturedArtists().addAll(allArtistsInSong);
                }

                // D. Lưu Album -> Hibernate sẽ update cả bảng album_song, album_genre, album_artist
                albumRepository.save(album);
            }

            // Set ngược lại cho song (chỉ để hiển thị)
            savedSong.setAlbums(albums);
        }

        // 9. Map sang Response và trả về
        List<Song> songs = new ArrayList<>();
        songs.add(savedSong);
        return flattenSongsWithAlbums(songs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SongResponse> update(Long id, SongRequest dto, MultipartFile musicFile, MultipartFile imgFile) throws IOException {
        // 1. Tìm bài hát
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài hát với ID: " + id));

        // 2. Update Title
        song.setTitle(dto.getTitle());

        // 3. Xử lý Audio File (Giữ nguyên logic cũ)
        if (musicFile != null && !musicFile.isEmpty()) {
            try {
                if (song.getSongUrl() != null) s3Service.deleteFileByUrl(song.getSongUrl());
            } catch (Exception e) { /* Log warning */ }

            String audioName = generateFileName(musicFile);
            String newUrl = s3Service.uploadCompressedFile(musicFile, "music/song", audioName, true, MAX_AUDIO_SIZE, MediaType.AUDIO);
            song.setSongUrl(newUrl);

            int newDuration = audioUtil.getDurationFromMusicFile(musicFile);
            if (newDuration > 0) song.setDuration(newDuration);
        }

        // 4. Xử lý Image File (Giữ nguyên logic cũ)
        if (imgFile != null && !imgFile.isEmpty()) {
            try {
                if (song.getImgUrl() != null) s3Service.deleteFileByUrl(song.getImgUrl());
            } catch (Exception e) { /* Log warning */ }

            String imgName = generateFileName(imgFile);
            String newImgUrl = s3Service.uploadFile(imgFile, "music/img", imgName, true, MAX_COVER_SIZE);
            song.setImgUrl(newImgUrl);
        }

        // 5. Update Genres (Giữ nguyên logic cũ)
        if (dto.getGenreIds() != null) {
            song.getGenres().clear();
            if (dto.getGenreIds().length > 0) {
                var genreIds = Arrays.asList(dto.getGenreIds());
                var newGenres = new HashSet<>(genreRepository.findAllById(genreIds));
                song.getGenres().addAll(newGenres);
            }
        }

        // 6. Update Artist (CẬP NHẬT MỚI: Xử lý theo Role động)
        // A. Xóa sạch role cũ trong DB
        songArtistRoleRepository.deleteAll(song.getArtistRoles());
        song.getArtistRoles().clear();

        List<SongArtistRole> newRoles = new ArrayList<>();
        Set<Long> processedArtistIds = new HashSet<>(); // Để check trùng

        // B. Main Artist (Luôn phải có)
        var mainArtist = artistRepository.findById(dto.getMainArtistId())
                .orElseThrow(() -> new RuntimeException("Main Artist not found"));

        newRoles.add(new SongArtistRole(null, song, mainArtist, ArtistRole.MAIN_ARTIST));
        processedArtistIds.add(mainArtist.getId());

        // C. Other Artists (Featured, Composer, Producer...)
        if (dto.getOtherArtists() != null) {
            for (SongArtistRequest artistReq : dto.getOtherArtists()) {
                Long artistId = artistReq.getArtistId();

                // Bỏ qua nếu trùng với Main Artist hoặc trùng lặp trong list
                if (processedArtistIds.contains(artistId)) continue;

                var artist = artistRepository.findById(artistId)
                        .orElseThrow(() -> new RuntimeException("Artist not found ID: " + artistId));

                // Set role động theo request
                newRoles.add(new SongArtistRole(null, song, artist, artistReq.getRole()));
                processedArtistIds.add(artistId);
            }
        }

        // Lưu batch và cập nhật reference
        songArtistRoleRepository.saveAll(newRoles);
        song.setArtistRoles(newRoles);

        // 7. Update Albums (Logic cũ vẫn hoạt động tốt với danh sách role mới)
        // Bước A: Gỡ khỏi album cũ
        if (song.getAlbums() != null && !song.getAlbums().isEmpty()) {
            for (Album oldAlbum : song.getAlbums()) {
                oldAlbum.getSongs().remove(song);
                albumRepository.save(oldAlbum);
            }
            song.getAlbums().clear();
        }

        // Bước B: Thêm vào album mới
        if (dto.getAlbumIds() != null && dto.getAlbumIds().length > 0) {
            var newAlbumIds = Arrays.asList(dto.getAlbumIds());
            var newAlbums = new HashSet<>(albumRepository.findAllById(newAlbumIds));

            // Lấy danh sách tất cả artist (bao gồm cả Composer, Producer...) để add vào Album
            List<Artist> allArtists = newRoles.stream().map(SongArtistRole::getArtist).toList();

            for (Album album : newAlbums) {
                album.getSongs().add(song);

                if (song.getGenres() != null) {
                    album.getGenres().addAll(song.getGenres());
                }

                if (!allArtists.isEmpty()) {
                    if (album.getFeaturedArtists() == null) album.setFeaturedArtists(new HashSet<>());
                    album.getFeaturedArtists().addAll(allArtists);
                    // Remove owner khỏi featured
                    if (album.getAlbumOwner() != null) album.getFeaturedArtists().remove(album.getAlbumOwner());
                }
                albumRepository.save(album);
            }
            song.setAlbums(newAlbums);
        }

        // 8. Save & Return
        Song updatedSong = songRepository.save(song);

        List<Song> songs = new ArrayList<>();
        songs.add(updatedSong);
        return flattenSongsWithAlbums(songs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDelete(Long id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài hát với ID: " + id));

        // Đánh dấu là đã xóa
        song.setDeleted(true);

        // Lưu lại trạng thái mới
        songRepository.save(song);

        // Tùy chọn: Nếu muốn user không tìm thấy bài hát này trong Album nữa,
        // bạn có thể remove nó khỏi album (logic giống hard delete) hoặc giữ nguyên.
        // Nếu giữ nguyên thì user vào Album vẫn thấy bài hát (trừ khi Album cũng lọc song deleted).
    }

    @Override
    @Transactional
    public void restore(Long id) {

        Song song = songRepository.findSoftDeletedSong(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài hát đã xóa mềm"));

        song.setDeleted(false);
        songRepository.save(song);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        // 1. Tìm bài hát (Nếu ko thấy thì báo lỗi)
        Song song = songRepository.findByIdIgnoringDeleted(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài hát với ID: " + id));

        // 2. Gỡ bài hát ra khỏi các Album (Quan trọng)
        // Vì Album là bên sở hữu quan hệ (Owner), ta nên cập nhật từ phía Album
        if (song.getAlbums() != null && !song.getAlbums().isEmpty()) {
            for (Album album : song.getAlbums()) {
                // Xóa song khỏi set songs của album
                album.getSongs().remove(song);
                // Lưu lại album để cập nhật bảng trung gian album_song
                albumRepository.save(album);
            }
        }

        // 3. Xóa file trên S3 (Dọn rác)
        // Bọc trong try-catch để lỡ S3 lỗi thì vẫn cho phép xóa DB (tùy nghiệp vụ, ở đây t để strict)
        try {
            if (song.getSongUrl() != null) {
                s3Service.deleteFileByUrl(song.getSongUrl());
            }
            if (song.getImgUrl() != null) {
                s3Service.deleteFileByUrl(song.getImgUrl());
            }
        } catch (Exception e) {
            // Tùy chọn: Throw lỗi để rollback DB nếu muốn bắt buộc xóa S3 thành công
            throw new RuntimeException("Lỗi khi xóa file trên S3: " + e.getMessage());
        }

        // 4. Xóa bài hát trong DB
        // Hibernate sẽ tự động xóa các dòng trong bảng con song_artist_role (do CascadeType.ALL)
        // và bảng trung gian song_genre.
        songRepository.delete(song);
    }

    // --- Helper Methods ---

    private String generateFileName(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        return UUID.randomUUID() + ext;
    }

    private List<SongResponse> flattenSongsWithAlbums(List<Song> songs) {
        List<SongResponse> result = new ArrayList<>();
        for (Song song : songs) {
            if (song.getAlbums() != null && !song.getAlbums().isEmpty()) {
                for (Album album : song.getAlbums()) {
                    result.add(mapToSongResponse(song, album));
                }
            } else {
                // Nếu song không có album, vẫn trả về song với album = null
                result.add(mapToSongResponse(song, null));
            }
        }
        return result;
    }

    @Transactional
    @Override
    public void increasePlayCount(Long songId) {
        var userId = currentUserProvider.get().getId();
        String redisKey = "play:" + userId + ":" + songId;

        // Nếu trong 30s đã nghe → không tăng tiếp
        Boolean alreadyPlayed = redis.hasKey(redisKey);
        if (Boolean.TRUE.equals(alreadyPlayed)) return;

        // Tăng playCount
        songRepository.incrementPlayCount(songId, userId);

        // Lấy song để log lịch sử
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("Song not found"));

        // Lưu lịch sử nghe
        songPlayHistoryRepository.save(
                SongPlayHistory.builder()
                        .song(song)
                        .userId(userId)
                        .playedAt(LocalDateTime.now())
                        .build()
        );
        // Set key Redis sống 30s → debounce
        redis.opsForValue().set(redisKey, "1", Duration.ofSeconds(30));
    }

    private SongResponse mapToSongResponse(Song song, Album album) {
        SongResponse response = new SongResponse();

        response.setId(song.getId());
        response.setTitle(song.getTitle());
        response.setDuration(song.getDuration());
        response.setPlayCount(song.getPlayCount());
        response.setSongUrl(song.getSongUrl());
        response.setCoverImageUrl(song.getImgUrl());

        List<SongArtistRole> roles = song.getArtistRoles();

        // Main Artist
        roles.stream()
                .filter(r -> r.getRole() == ArtistRole.MAIN_ARTIST)
                .findFirst()
                .ifPresent(r -> response.setMainArtist(
                        new ArtistSummaryDto(
                                r.getArtist().getId(),
                                r.getArtist().getName(),
                                ArtistRole.MAIN_ARTIST,
                                r.getArtist().getImgUrl()
                        )
                ));

        List<ArtistSummaryDto> otherArtists = roles.stream()
                .filter(r -> r.getRole() != ArtistRole.MAIN_ARTIST)
                .map(r -> new ArtistSummaryDto(
                        r.getArtist().getId(),
                        r.getArtist().getName(),
                        r.getRole(),
                        r.getArtist().getImgUrl()
                ))
                .collect(Collectors.toList());
        response.setOtherArtists(otherArtists);

        if (song.getGenres() != null) {
            List<GenreDto> genreDtos = song.getGenres().stream()
                    .map(g -> new GenreDto(g.getId(), g.getName()))
                    .collect(Collectors.toList());
            response.setGenres(genreDtos);
        }

        if (album != null) {
            response.setAlbum(new AlbumSummaryDto(album.getId(), album.getTitle(), album.getPlayCount()));
        }

        return response;
    }

    private SongResponseWithAllAlbum mapToSongResponseWithAllAlbums(Song song) {
        SongResponseWithAllAlbum response = new SongResponseWithAllAlbum();

        // 1. Basic Info
        response.setId(song.getId());
        response.setTitle(song.getTitle());
        response.setDuration(song.getDuration());
        response.setPlayCount(song.getPlayCount());
        response.setSongUrl(song.getSongUrl());
        response.setCoverImageUrl(song.getImgUrl());

        List<SongArtistRole> roles = song.getArtistRoles();

        // 2. Main Artist
        roles.stream()
                .filter(r -> r.getRole() == ArtistRole.MAIN_ARTIST)
                .findFirst()
                .ifPresent(r -> response.setMainArtist(
                        new ArtistSummaryDto(
                                r.getArtist().getId(),
                                r.getArtist().getName(),
                                ArtistRole.MAIN_ARTIST,
                                r.getArtist().getImgUrl()
                                // Set role cứng hoặc lấy r.getRole()
                        )
                ));

        // 3. Other Artists (Featured, Composer, Producer...)
        // SỬA: Lấy tất cả trừ Main và map Role động vào DTO
        List<ArtistSummaryDto> otherArtists = roles.stream()
                .filter(r -> r.getRole() != ArtistRole.MAIN_ARTIST)
                .map(r -> new ArtistSummaryDto(
                        r.getArtist().getId(),
                        r.getArtist().getName(),
                        r.getRole(),
                        r.getArtist().getImgUrl()
                        // <--- QUAN TRỌNG: Lấy role thực tế từ DB
                ))
                .collect(Collectors.toList());

        // Giả sử DTO của bạn tên field là otherArtists (hoặc featuredArtists tùy bạn đặt)
        response.setOtherArtists(otherArtists);

        // 4. Genres
        List<GenreDto> genreDtos = song.getGenres().stream()
                .map(g -> new GenreDto(g.getId(), g.getName()))
                .collect(Collectors.toList());
        response.setGenres(genreDtos);

        // 5. Albums (List All)
        if (song.getAlbums() != null && !song.getAlbums().isEmpty()) {
            List<AlbumSummaryDto> albumSummaries = song.getAlbums().stream()
                    .map(a -> new AlbumSummaryDto(a.getId(), a.getTitle(), a.getPlayCount()))
                    .collect(Collectors.toList());
            response.setAlbums(albumSummaries);
        } else {
            response.setAlbums(new ArrayList<>()); // Trả về list rỗng thay vì null cho an toàn
        }

        return response;
    }
}
