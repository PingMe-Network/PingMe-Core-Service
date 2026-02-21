package org.ping_me.controller.ai.transcribe;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ping_me.dto.base.ApiResponse;
import org.ping_me.service.ai.transcribe.SpeechToTextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.ping_me.advice.base.ErrorCode.UNCATEGORIZED_EXCEPTION;

/**
 * @author Le Tran Gia Huy
 * @created 16/02/2026 - 10:19 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.controller.ai.transcribe
 */

@Tag(
        name = "Speech To Text",
        description = "Quản lý các chức năng liên quan đến chuyển đổi giọng nói thành văn bản - Sử dụng model AI của OpenAI để thực hiện chức năng này"
)
@RestController
@RequestMapping("/transcribe")
@RequiredArgsConstructor
public class SpeechToTextController {
    private final SpeechToTextService groqService;

    @PostMapping(value = "/audio", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> transcribe(@RequestParam("file") MultipartFile file) {
        try {
            String text = groqService.transcribeAudio(file);
            return ResponseEntity.ok(new ApiResponse<>(text));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(UNCATEGORIZED_EXCEPTION.getMessage(), UNCATEGORIZED_EXCEPTION.getCode()));
        }
    }
}
