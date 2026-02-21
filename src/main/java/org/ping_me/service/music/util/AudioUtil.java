package org.ping_me.service.music.util;

import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author Le Tran Gia Huy
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 */
@Slf4j
@Component
public class AudioUtil {

    public int getDurationFromMusicFile(MultipartFile file) {
        File tempFile = null;
        try {
            // 1. Lấy thư mục tạm an toàn (Fix S5443)
            File secureDir = getSecureTempDir();

            // 2. Sanitize tên file: Chỉ lấy extension (Fix S6549)
            // Không nối file.getOriginalFilename() trực tiếp
            String originalName = file.getOriginalFilename();
            String extension = ".tmp";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            // 3. Tạo file tạm TRONG thư mục an toàn
            // File.createTempFile sẽ tự sinh tên ngẫu nhiên (vd: audio_duration_12345.mp3)
            tempFile = File.createTempFile("audio_duration_", extension, secureDir);

            Files.copy(file.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 4. Đọc thông tin file
            AudioFile audioFile = AudioFileIO.read(tempFile);
            return audioFile.getAudioHeader().getTrackLength();

        } catch (Exception e) {
            log.error("Lỗi khi đọc duration file [{}]: {}", file.getOriginalFilename(), e.getMessage());
            return 0;
        } finally {
            deleteTempFile(tempFile);
        }
    }

    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            boolean isDeleted = file.delete();
            if (!isDeleted)
                log.warn("CẢNH BÁO: Không thể xóa file tạm: {}", file.getAbsolutePath());
        }
    }

    /**
     * Copy logic tạo thư mục an toàn từ FFMPEGService qua
     */
    private File getSecureTempDir() throws IOException {
        String systemTemp = System.getProperty("java.io.tmpdir");
        // Đặt tên folder khác chút để dễ phân biệt, ví dụ: pingme-audio-temp
        Path path = Paths.get(systemTemp, "pingme-audio-temp");

        if (!Files.exists(path)) {
            Files.createDirectories(path);
            File file = path.toFile();

            boolean r = file.setReadable(true, true);
            boolean w = file.setWritable(true, true);
            boolean x = file.setExecutable(true, true);

            if (!r || !w || !x) {
                if (file.exists() && !file.delete()) {
                    log.warn("CẢNH BÁO: Không thể xóa thư mục tạm không an toàn: {}", file.getAbsolutePath());
                }
                throw new IOException("Lỗi bảo mật: Không thể thiết lập quyền hạn chế (700) cho thư mục tạm: " + file.getAbsolutePath());
            }
        }
        return path.toFile();
    }
}