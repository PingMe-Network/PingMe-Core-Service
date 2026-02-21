package org.ping_me.service.ai.transcribe.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ping_me.service.ai.transcribe.SpeechToTextService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Le Tran Gia Huy
 * @created 16/02/2026 - 10:23 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.service.ai.transcribe
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class SpeechToTextServiceImpl implements SpeechToTextService {
    @Value("${groq.ai.api.key}")
    private String apiKey;
    @Value("${groq.ai.api.url}")
    private String apiUrl;
    private final RestClient restClient;

    public SpeechToTextServiceImpl() {
        this.restClient = RestClient.builder().build();
    }

    public String transcribeAudio(MultipartFile audioFile) throws IOException {
        // 1. Chuẩn bị Body (Multipart)
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Resource từ file upload
        ByteArrayResource fileResource = new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                return audioFile.getOriginalFilename() != null ? audioFile.getOriginalFilename() : "audio.webm";
            }
        };

        body.add("file", fileResource);
        body.add("model", "whisper-large-v3");
        body.add("language", "vi"); // Tùy chọn: Gợi ý ngôn ngữ (nếu muốn force tiếng Việt)

        // 2. Gọi API Groq
        GroqResponse response = restClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(GroqResponse.class);

        return response != null ? response.text() : "";
    }

    // Record class để map response JSON
    public record GroqResponse(String text) {}
}
