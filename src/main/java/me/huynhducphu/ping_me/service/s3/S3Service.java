package me.huynhducphu.ping_me.service.s3;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import me.huynhducphu.ping_me.advice.exception.S3UploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Admin 8/16/2025
 **/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class S3Service {

    S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    @NonFinal
    String awsBucketName;

    @Value("${aws.s3.domain}")
    @NonFinal
    String domain;

    /**
     * Tải tệp lên S3
     */
    public String uploadFile(
            MultipartFile file, String key,
            boolean getUrl, long maxFileSize
    ) {
        try {
            validateFile(file, maxFileSize);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(awsBucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

            return getUrl ? domain + "/" + key : key;

        } catch (Exception e) {
            throw new S3UploadException("Không tải được dữ liệu tệp: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String uploadFile(
            MultipartFile file, String folder,
            String fileName, boolean getUrl,
            long maxFileSize
    ) {
        String key = String.format("%s/%s", folder, fileName);
        return uploadFile(file, key, getUrl, maxFileSize);
    }

    /**
     * Xóa tệp bằng Key (Dùng cho logic nội bộ)
     */
    public void deleteFileByKey(String key) {
        try {
            if (key == null || key.isBlank()) {
                throw new S3UploadException("Key không hợp lệ", HttpStatus.NOT_FOUND);
            }

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(awsBucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new S3UploadException("Lỗi khi xóa tệp trên S3", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Xóa tệp bằng URL hoàn chỉnh (Tự động bóc tách Key từ Domain)
     */
    public void deleteFileByUrl(String url) {
        String base = domain + "/";

        if (!url.startsWith(base)) {
            throw new S3UploadException("URL không thuộc hệ thống lưu trữ hiện tại", HttpStatus.BAD_REQUEST);
        }

        String key = url.substring(base.length());
        deleteFileByKey(key);
    }

    private void validateFile(MultipartFile file, long maxFileSize) {
        if (file == null || file.isEmpty())
            throw new S3UploadException("Tệp gửi lên bị rỗng", HttpStatus.BAD_REQUEST);

        if (file.getSize() > maxFileSize)
            throw new S3UploadException("Tệp quá lớn (> " + maxFileSize + " bytes)", HttpStatus.CONTENT_TOO_LARGE);
    }

}
