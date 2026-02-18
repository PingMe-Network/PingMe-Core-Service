package me.huynhducphu.ping_me.service.ai.transcribe;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Le Tran Gia Huy
 * @created 16/02/2026 - 10:24 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.service.ai.transcribe.impl
 */
public interface SpeechToTextService {
    String transcribeAudio(MultipartFile audioFile) throws IOException;
}
