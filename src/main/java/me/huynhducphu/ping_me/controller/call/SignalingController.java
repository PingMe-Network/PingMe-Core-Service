package me.huynhducphu.ping_me.controller.call;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.request.call.SignalingRequest;
import me.huynhducphu.ping_me.service.call.impl.SignalingServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Le Tran Gia Huy
 * @created 01/12/2025 - 5:56 PM
 */
@Tag(
        name = "Call Signaling",
        description = "Các endpoints xử lý tín hiệu cuộc gọi (WebRTC / Call realtime)"
)
@RestController
@RequestMapping("/chat/signaling")
@RequiredArgsConstructor
public class SignalingController {

    private final SignalingServiceImpl signalingService;

    // ================= SIGNAL =================
    @Operation(
            summary = "Gửi tín hiệu cuộc gọi",
            description = """
                    Gửi signaling message phục vụ thiết lập / duy trì cuộc gọi realtime.
                    Dùng cho các loại tín hiệu như:
                    - offer
                    - answer
                    - ice-candidate
                    """
    )
    @PostMapping
    public ResponseEntity<Void> sendSignal(
            @Parameter(
                    description = "Payload signaling (SDP / ICE / metadata)",
                    required = true
            )
            @RequestBody SignalingRequest request
    ) {
        signalingService.processSignaling(request);
        return ResponseEntity.ok().build();
    }
}
