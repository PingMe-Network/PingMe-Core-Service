package me.huynhducphu.ping_me.service.music.impl;

import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.request.music.AlbumRequest;
import me.huynhducphu.ping_me.dto.response.music.AlbumResponse;
import me.huynhducphu.ping_me.model.music.Album;
import me.huynhducphu.ping_me.model.music.Artist;
import me.huynhducphu.ping_me.repository.jpa.music.AlbumRepository;
import me.huynhducphu.ping_me.repository.jpa.music.ArtistRepository;
import me.huynhducphu.ping_me.service.music.AlbumService;
import me.huynhducphu.ping_me.service.s3.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final S3Service s3Service;

    @Override
    public Page<AlbumResponse> getAllAlbums(Pageable pageable) {
         Page<Album> albumPage = albumRepository.findAll(pageable);
         return albumPage.map(this::mapToResponse);
    }

    @Override
    public AlbumResponse getAlbumById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Album với ID: " + id));
        return mapToResponse(album);
    }

    @Override
    public Page<AlbumResponse> getAlbumByTitleContainIgnoreCase(String title, Pageable pageable) {
        Page<Album> albums = albumRepository.findAlbumsByTitleContainingIgnoreCase(title, pageable);
        return albums.map(this::mapToResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlbumResponse save(
            AlbumRequest albumRequestDto,
            MultipartFile albumCoverImg
    ) {
        // 1. Validate ảnh
        if (albumCoverImg == null || albumCoverImg.isEmpty()) {
            throw new RuntimeException("Vui lòng tải lên ảnh bìa");
        }

        var album = new Album();

        // 2. Set Title
        album.setTitle(albumRequestDto.getTitle());

        // 3. Set Owner (Dùng findById an toàn hơn)
        Artist owner = artistRepository.findById(albumRequestDto.getAlbumOwnerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Artist với ID: " + albumRequestDto.getAlbumOwnerId()));
        album.setAlbumOwner(owner);

        album.setPlayCount(0L);

        // 4. Upload ảnh
        String coverName = generateFileName(albumCoverImg);
        String coverUrl = s3Service.uploadFile(
                albumCoverImg,
                "music/img",
                coverName,
                true,
                MAX_COVER_SIZE
        );
        album.setCoverImageUrl(coverUrl);

        // 5. Init Set rỗng (Chuẩn)
        album.setGenres(new HashSet<>());
        album.setSongs(new HashSet<>());
        album.setFeaturedArtists(new HashSet<>());

        // 6. Lưu và trả về
        var savedAlbum = albumRepository.save(album);
        return mapToResponse(savedAlbum);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlbumResponse update(Long albumId, AlbumRequest albumRequestDto, MultipartFile albumCoverImg) {
        var album = albumRepository.findById(albumId).orElseThrow(
                () -> new RuntimeException("Không tìm thấy album với ID: " + albumId)
        );

        if (albumRequestDto.getTitle() != null && !albumRequestDto.getTitle().isEmpty()) {
            album.setTitle(albumRequestDto.getTitle());
        }

        if (albumRequestDto.getAlbumOwnerId() != null) {
            Artist owner = artistRepository.findById(albumRequestDto.getAlbumOwnerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Artist mới"));
            album.setAlbumOwner(owner);
        }

        if (albumCoverImg != null && !albumCoverImg.isEmpty()) {
            // SỬA: Bọc try-catch để an toàn luồng
            try {
                if (album.getCoverImageUrl() != null) s3Service.deleteFileByUrl(album.getCoverImageUrl());
            } catch (Exception e) { /* Log warning */ }

            String coverName = generateFileName(albumCoverImg);
            String newCoverUrl = s3Service.uploadFile(albumCoverImg, "music/img", coverName, true, MAX_COVER_SIZE);
            album.setCoverImageUrl(newCoverUrl);
        }

        var savedAlbum = albumRepository.save(album);
        return mapToResponse(savedAlbum);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDelete(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Album"));

        album.setDeleted(true);
        albumRepository.save(album);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // THÊM Transactional
    public void hardDelete(Long id) {
        // Tìm cả album đã xóa mềm
        Album album = albumRepository.findByIdIgnoringDeleted(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Album"));

        // SỬA: Xóa ảnh trên S3 để dọn rác
        try {
            if (album.getCoverImageUrl() != null) {
                s3Service.deleteFileByUrl(album.getCoverImageUrl());
            }
        } catch (Exception e) {
            // Log lỗi, không chặn xóa DB (tùy nghiệp vụ)
            System.err.println("Lỗi xóa ảnh S3: " + e.getMessage());
        }

        // Xóa DB (Hibernate tự xóa dòng trong bảng trung gian)
        albumRepository.delete(album);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // THÊM Transactional
    public void restore(Long id) {
        // Nên dùng hàm findSoftDeletedAlbum để chắc chắn đang restore cái đã xóa
        // Nhưng findByIdIgnoringDeleted của bạn cũng chạy được
        Album album = albumRepository.findByIdIgnoringDeleted(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy album"));

        album.setDeleted(false);
        albumRepository.save(album);
    }

    // --- Helper Methods ---

    private AlbumResponse mapToResponse(Album album) {
        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getCoverImageUrl(),
                album.getPlayCount()
        );
    }

    private String generateFileName(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        return UUID.randomUUID() + ext;
    }
}