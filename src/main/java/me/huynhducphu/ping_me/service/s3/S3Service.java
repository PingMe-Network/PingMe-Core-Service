package me.huynhducphu.ping_me.service.s3;

import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.advice.exception.S3UploadException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

/**
 * Admin 8/16/2025
 **/
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    private final String awsBucketName;
    private final String awsRegion;

    public String uploadFile(
            MultipartFile file, String key,
            boolean getUrl, long maxFileSize
    ) {
        try {
            if (file == null || file.isEmpty())
                throw new S3UploadException(
                        "Tệp được gửi lên bị rỗng",
                        HttpStatus.BAD_REQUEST
                );

            if (file.getSize() > maxFileSize)
                throw new S3UploadException(
                        "Tệp quá lớn (> " + maxFileSize + " bytes)",
                        HttpStatus.CONTENT_TOO_LARGE
                );

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(awsBucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

            if (getUrl)
                return String.format("https://%s.s3.%s.amazonaws.com/%s", awsBucketName, awsRegion, key);
            else return key;
        } catch (Exception e) {
            throw new S3UploadException(
                    "Không tải được dữ liệu tệp",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
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

    public void deleteFileByKey(String key) {
        try {
            if (key == null)
                throw new S3UploadException(
                        "Không tìm thấy tệp cần xóa",
                        HttpStatus.NOT_FOUND
                );

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(awsBucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);

        } catch (Exception e) {
            throw new S3UploadException(
                    "Không thể xóa tệp",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    public void deleteFileByUrl(String url) {
        String base = String.format("https://%s.s3.%s.amazonaws.com/", awsBucketName, awsRegion);
        if (!url.startsWith(base)) {
            throw new S3UploadException(
                    "URL không hợp lệ",
                    HttpStatus.BAD_REQUEST
            );
        }

        String key = url.substring(base.length());

        deleteFileByKey(key);
    }
}
