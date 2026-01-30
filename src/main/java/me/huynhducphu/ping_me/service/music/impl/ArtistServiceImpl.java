package me.huynhducphu.ping_me.service.music.impl;

import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.request.music.ArtistRequest;
import me.huynhducphu.ping_me.dto.response.music.ArtistResponse;
import me.huynhducphu.ping_me.model.music.Artist;
import me.huynhducphu.ping_me.repository.jpa.music.ArtistRepository;
import me.huynhducphu.ping_me.service.music.ArtistService;
import me.huynhducphu.ping_me.service.s3.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final S3Service s3Service;

    private static final Long MAX_IMG_SIZE = 5L * 1024L * 1024L; // 5MB

    @Override
    public Page<ArtistResponse> getAllArtists(Pageable pageable) {
        Page<Artist> artistPage = artistRepository.findAll(pageable);
        return artistPage.map(this::mapToResponse);
    }

    @Override
    public Page<ArtistResponse> searchArtists(String name, Pageable pageable) {
        Page<Artist> artistPage = artistRepository.findByNameContainingIgnoreCase(name, pageable);
        return artistPage.map(this::mapToResponse);
    }

    @Override
    public ArtistResponse getArtistById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nghệ sĩ với ID: " + id));
        return mapToResponse(artist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArtistResponse saveArtist(ArtistRequest request, MultipartFile imgFile) {
        // 1. Validate ảnh
        if (imgFile == null || imgFile.isEmpty()) {
            throw new RuntimeException("Vui lòng tải lên ảnh đại diện nghệ sĩ");
        }

        var artist = new Artist();
        artist.setName(request.getName());
        artist.setBio(request.getBio());

        // 2. Upload S3
        String fileName = generateFileName(imgFile);
        String imgUrl = s3Service.uploadFile(imgFile, "music/img", fileName, true, MAX_IMG_SIZE);
        artist.setImgUrl(imgUrl);

        // 3. Save DB
        var savedArtist = artistRepository.save(artist);
        return mapToResponse(savedArtist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArtistResponse updateArtist(Long id, ArtistRequest request, MultipartFile imgFile) {
        // 1. Tìm nghệ sĩ
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nghệ sĩ với ID: " + id));

        // 2. Update thông tin text
        if (request.getName() != null && !request.getName().isBlank()) {
            artist.setName(request.getName());
        }
        if (request.getBio() != null) {
            artist.setBio(request.getBio());
        }

        // 3. Update ảnh (Nếu có file mới)
        if (imgFile != null && !imgFile.isEmpty()) {
            // Xóa ảnh cũ trên S3
            try {
                if (artist.getImgUrl() != null) s3Service.deleteFileByUrl(artist.getImgUrl());
            } catch (Exception e) { /* Log warning */ }

            // Upload ảnh mới
            String fileName = generateFileName(imgFile);
            String newUrl = s3Service.uploadFile(imgFile, "music/img", fileName, true, MAX_IMG_SIZE);
            artist.setImgUrl(newUrl);
        }

        // 4. Save DB
        var updatedArtist = artistRepository.save(artist);
        return mapToResponse(updatedArtist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDeleteArtist(Long id) {
        // 1. Tìm Artist (đang active)
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nghệ sĩ với ID: " + id));

        // 2. CHECK RÀNG BUỘC (Vẫn cần thiết!)
        // Nếu xóa mềm Artist, các Album/Song liên quan sẽ bị mồ côi (Orphan).
        // Tùy nghiệp vụ, bạn có 2 lựa chọn:
        // Opt A: Bắt buộc xóa hết nhạc trước rồi mới cho xóa người (An toàn nhất).
        // Opt B: Tự động Soft Delete luôn tất cả Album của người này (Cascade Soft Delete).

        // Ở đây t chọn Opt A:
        if (artistRepository.hasOwnedAlbums(id)) {
            throw new RuntimeException("Không thể xóa nghệ sĩ này vì họ đang sở hữu Album.");
        }
        if (artistRepository.hasSongRoles(id)) {
            throw new RuntimeException("Không thể xóa nghệ sĩ này vì họ đang có bài hát.");
        }

        // 3. Đánh dấu xóa
        artist.setDeleted(true);
        artistRepository.save(artist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreArtist(Long id) {
        // Tìm trong thùng rác
        Artist artist = artistRepository.findSoftDeletedArtist(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nghệ sĩ đã xóa mềm"));

        artist.setDeleted(false);
        artistRepository.save(artist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteArtist(Long id) {
        // Tìm bất kể trạng thái
        Artist artist = artistRepository.findByIdIgnoringDeleted(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nghệ sĩ"));

        // Check ràng buộc lần nữa cho chắc (tránh lỗi Foreign Key DB)
        if (artistRepository.hasOwnedAlbums(id) || artistRepository.hasSongRoles(id)) {
            throw new RuntimeException("Vẫn còn dữ liệu liên quan, không thể xóa vĩnh viễn!");
        }

        // 1. Xóa ảnh S3
        try {
            if (artist.getImgUrl() != null) s3Service.deleteFileByUrl(artist.getImgUrl());
        } catch (Exception e) {
            System.err.println("Lỗi xóa ảnh S3: " + e.getMessage());
        }

        // 2. Xóa vĩnh viễn trong DB
        artistRepository.delete(artist);
    }


    // --- Helper Methods ---

    private ArtistResponse mapToResponse(Artist artist) {
        return new ArtistResponse(
                artist.getId(),
                artist.getName(),
                artist.getBio(),
                artist.getImgUrl()
        );
    }

    private String generateFileName(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf("."))
                : "";
        return UUID.randomUUID() + ext;
    }
}