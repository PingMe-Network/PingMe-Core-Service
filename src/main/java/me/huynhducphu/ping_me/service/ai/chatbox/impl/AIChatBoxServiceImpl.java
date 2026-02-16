package me.huynhducphu.ping_me.service.ai.chatbox.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.huynhducphu.ping_me.dto.response.ai.AIChatResponseDTO;
import me.huynhducphu.ping_me.dto.response.ai.AIChatRoomInformationDTO;
import me.huynhducphu.ping_me.model.ai.AIChatRoom;
import me.huynhducphu.ping_me.model.ai.AIMessage;
import me.huynhducphu.ping_me.model.constant.AIMessageType;
import me.huynhducphu.ping_me.repository.mongodb.ai.AIChatRoomRepository;
import me.huynhducphu.ping_me.repository.mongodb.ai.AIMessageRepository;
import me.huynhducphu.ping_me.service.ai.chatbox.AIChatBoxService;
import me.huynhducphu.ping_me.service.s3.S3Service;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import me.huynhducphu.ping_me.utils.AIChatHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Le Tran Gia Huy
 * @created 27/01/2026 - 5:54 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.service.ai.chatbox
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class AIChatBoxServiceImpl implements AIChatBoxService {

    private static final long MAX_IMG_SIZE = 5L * 1024L * 1024L;
    private final S3Service s3Service;
    private final AIMessageRepository aiMessageRepository;
    private final AIChatRoomRepository aiChatRoomRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ChatSummarizerAsyncService chatSummarizerService;
    private final ChatTitleAsyncService chatTitleAsyncService;
    private final AIChatHelper aiChatHelper;

    public Slice<AIMessage> getChatHistory(UUID chatRoomId, int pageNumber, int pageSize) {
        Long currentUserId = currentUserProvider.get().getId();
        // Bước 1: Kiểm tra quyền truy cập (Bảo mật)
        // Phải đảm bảo phòng chat này tồn tại VÀ thuộc về user đang đăng nhập
        AIChatRoom room = aiChatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Phòng chat không tồn tại!"));
        if (!room.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn không có quyền xem tin nhắn này!");
        }
        if (pageSize % 2 != 0) {
            pageSize += 1; //đảm bảo pageSize luôn là số chẵn để phân bổ đều tin nhắn giữa User và AI
        }
        // Bước 2: Lấy tin nhắn (Của cả User VÀ AI)
        // Trả về Slice (Thay vì List)
        // Slice chứa: List tin nhắn + biến 'hasNext' (còn trang sau ko)
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return aiMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);
    }

    public Slice<AIChatRoomInformationDTO> getUserChatRooms(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Long userId = currentUserProvider.get().getId();
        Slice<AIChatRoom> chatRoomsSlice = aiChatRoomRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
        return chatRoomsSlice.map(this::mappingFromAIChatRoomToAIChatRoomInformationResponse);
    }

    public AIChatResponseDTO sendMessageToAI(UUID chatRoomId, String prompt, List<MultipartFile> files) {
        //Kiểm tra tính hợp lệ của file
        validateFiles(files);
        validatePrompt(prompt);
        Long userId = currentUserProvider.get().getId();
        boolean isNewRoom = false;
        if (chatRoomId == null) {
            isNewRoom = true;
            AIChatRoom newChatRoom = new AIChatRoom();
            newChatRoom.setId(UUID.randomUUID());
            newChatRoom.setUserId(userId);
            // Lưu chat room mới vào database
            chatRoomId = newChatRoom.getId();
            aiChatRoomRepository.save(newChatRoom);
        } else if (!aiChatRoomRepository.existsById(chatRoomId)) {
            throw new EntityNotFoundException("Chat room không tồn tại!");
        }
        // Tải file lên S3 và lưu URL vào database
        List<AIMessage.Attachment> dbAttachments = processingMediaFile(files);
        //tạo 1 AIMessage lưu prompt và media vào database
        AIMessage humanMessage = new AIMessage();
        humanMessage.setId(UUID.randomUUID());
        humanMessage.setChatRoomId(chatRoomId);
        humanMessage.setUserId(userId);
        humanMessage.setType(AIMessageType.SENT);
        humanMessage.setContent(prompt);
        humanMessage.setAttachments(dbAttachments);
        aiMessageRepository.save(humanMessage);
        /**
         *Tạo 1 UserMessage bao gồm prompt và media. UserMessage thuộc Spring (org.springframework.ai.chat.messages)
         *Từ đó spring gửi cả UserMessage đó đến OpenAI thông qua ChatClient
         */
        //Gọi ChatClient để gửi prompt và nhận phản hồi từ AI
        String response = getResponseFromAI(userId, chatRoomId, prompt, files);
        AIMessage aiMessage = new AIMessage();
        aiMessage.setId(UUID.randomUUID());
        aiMessage.setChatRoomId(chatRoomId);
        aiMessage.setUserId(userId);
        aiMessage.setType(AIMessageType.RECEIVED);
        aiMessage.setContent(response);
        aiMessageRepository.save(aiMessage);
        // Cập nhật thông tin phòng chat
        AIChatRoom currentChatRoom = aiChatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Chat room không tồn tại!"));
        currentChatRoom.setInteractCountSinceLastSummary(currentChatRoom.getInteractCountSinceLastSummary() + 1);
        currentChatRoom.setTotalMsgCount(currentChatRoom.getTotalMsgCount() + 2);
        aiChatRoomRepository.save(currentChatRoom);
        if (currentChatRoom.getTitle() == null) {
            chatTitleAsyncService.generateAndBroadcastTitle(chatRoomId, prompt, response);
        } else {
            // Cứ sau mỗi 10 tin nhắn, ta sẽ yêu cầu GPT-5 Nano tóm tắt cuộc trò chuyện để làm ký ức dài hạn
            if (currentChatRoom.getInteractCountSinceLastSummary() >= 10) {
                // Gọi hàm tóm tắt hội thoại (chạy ngầm, không block luồng chính)
                chatSummarizerService.checkAndSummarize(chatRoomId);
            }
        }
        return mappingFromAIMessageToAIChatResponseDTO(response, chatRoomId, isNewRoom);
    }

    private String getResponseFromAI(Long userId, UUID currentRoomId, String prompt, List<MultipartFile> files) {
        // 1. Lấy 20 tin nhắn
        List<AIMessage> currentRoomMsgs = aiChatHelper.getCurrentRoomHistory(currentRoomId, 0, 20);
        // 2. Lấy 10 tin nhắn phòng khác
        List<AIMessage> otherRoomsMsgs = getOtherMessageHistoryFromAnotherRooms(userId, currentRoomId, 0, 10);
        Collections.reverse(currentRoomMsgs);
        Collections.reverse(otherRoomsMsgs);
        String currentSummary = aiChatRoomRepository.findById(currentRoomId)
                .map(AIChatRoom::getLatestSummary)
                .orElse("");
        String globalSummaries = getGlobalSummaries(userId, currentRoomId);
        String combinedSummary = "Current Room Summary: " + currentSummary + "\n\n" +
                "Other Rooms Summaries:\n" + globalSummaries;
        String context = buildSystemContext(
                currentRoomMsgs,
                otherRoomsMsgs,
                combinedSummary
        );
        return aiChatHelper.useAiWithContext(prompt, context, files, "gpt-4o-mini", 1000);
    }

    private List<AIMessage> getOtherMessageHistoryFromAnotherRooms(
            Long userId,
            UUID currentRoomId,
            int pageNumber,
            int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<AIMessage> otherRoomsMsgs = new ArrayList<>(
                aiMessageRepository
                        .findByUserIdAndChatRoomIdNotOrderByCreatedAtDesc(userId, currentRoomId, pageable)
                        .getContent()
        );
        Collections.reverse(otherRoomsMsgs);
        return otherRoomsMsgs;
    }

    private String buildSystemContext(
            List<AIMessage> currentRoomHistory,
            List<AIMessage> otherRoomsHistory,
            String summary
    ) {
        StringBuilder prompt = new StringBuilder();

        // 1. System Instruction & Critical Rules (Luật thép)
        prompt.append("System: Bạn là trợ lý AI hữu ích trong ứng dụng PingMe.\n");
        prompt.append("Nhiệm vụ: Trả lời câu hỏi người dùng dựa trên các ngữ cảnh được cung cấp dưới đây.\n\n");
        prompt.append("<critical_rules>\n");
        prompt.append("1. ƯU TIÊN HIỆN TẠI: Input và Ảnh (nếu có) ở lượt chat này là sự thật duy nhất. \n");
        prompt.append("2. QUÊN LỖI CŨ: Nếu lịch sử có câu 'tôi không thấy ảnh', hãy coi đó là lỗi quá khứ và BỎ QUA nó. \n");
        prompt.append("3. XỬ LÝ ẢNH: Nếu request này có ảnh, bạn BẮT BUỘC phải phân tích nó.\n");
        prompt.append("</critical_rules>\n\n");
        // 2. Ký ức dài hạn
        prompt.append("<long_term_memory>\n");
        prompt.append(summary != null && !summary.isEmpty() ? summary : "Chưa có tóm tắt nào.");
        prompt.append("\n</long_term_memory>\n\n");
        // 3. Ngữ cảnh chéo (Thêm check null để an toàn)
        if (otherRoomsHistory != null && !otherRoomsHistory.isEmpty()) {
            prompt.append("<related_context_from_other_rooms>\n");
            prompt.append(formatHistory(otherRoomsHistory));
            prompt.append("</related_context_from_other_rooms>\n\n");
        }
        // 4. Lịch sử hiện tại
        prompt.append("<current_conversation_history>\n");
        prompt.append(formatHistory(currentRoomHistory));
        prompt.append("</current_conversation_history>\n\n");
        // 5. [QUAN TRỌNG] Lời nhắc cuối cùng (Trigger)
        // Gom hết các lời dặn dò vào đây và chốt hạ bằng lệnh "Trả lời ngay"
        prompt.append("\nLƯU Ý CUỐI CÙNG: Tuyệt đối không lặp lại các câu trả lời rập khuôn từ lịch sử.\n");
        prompt.append("HÃY TRẢ LỜI NGAY BÂY GIỜ dựa trên input mới nhất. Nếu có ảnh, hãy mô tả chi tiết.");
        log.info("B5 : Built Prompt for AI:\n" + prompt);
        return prompt.toString();
    }

    // Helper: Chuyển List<AIMessage> thành String dễ đọc
    private String formatHistory(List<AIMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "(Không có dữ liệu)\n";
        }
        StringBuilder historyBuilder = new StringBuilder();
        for (AIMessage msg : messages) {
            // Xác định vai trò: User hay AI
            String role = (msg.getType() == AIMessageType.SENT) ? "User" : "AI";
            // Format: [User]: Nội dung tin nhắn
            historyBuilder.append(String.format("[%s]: %s\n", role, msg.getContent()));
        }
        return historyBuilder.toString();
    }

    private void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            String contentType = file.getContentType();
            long sizeInBytes = file.getSize();

            // 1. Chỉ cho phép các định dạng ảnh phổ biến
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Chỉ được phép tải lên hình ảnh! File "
                        + file.getOriginalFilename() + " không hợp lệ.");
            }

            // 2. Giới hạn dung lượng ảnh tối đa 5MB
            if (sizeInBytes > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Ảnh " + file.getOriginalFilename()
                        + " vượt quá giới hạn 5MB.");
            }
        }
    }

    private void validatePrompt(String prompt) {
        if (prompt.length() > 4000) {
            throw new IllegalArgumentException("Câu hỏi quá dài! Vui lòng tóm tắt lại dưới 4000 ký tự.");
        }
    }

    private String generateFileName(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        return UUID.randomUUID() + ext;
    }

    private String getGlobalSummaries(Long userId, UUID currentRoomId) {
        List<AIChatRoom> otherRooms = aiChatRoomRepository
                .findTop5ByUserIdAndIdNotOrderByUpdatedAtDesc(userId, currentRoomId);

        if (otherRooms.isEmpty()) return "";

        return otherRooms.stream()
                .filter(room -> StringUtils.hasText(room.getLatestSummary())) // Chỉ lấy phòng có summary
                .map(room -> String.format("- Chủ đề '%s': %s", room.getTitle(), room.getLatestSummary()))
                .collect(Collectors.joining("\n"));
    }

    private AIChatRoomInformationDTO mappingFromAIChatRoomToAIChatRoomInformationResponse(AIChatRoom room) {
        String displayTitle = (room.getTitle() != null && !room.getTitle().isEmpty())
                ? room.getTitle()
                : "Đang tạo tiêu đề...";
        return AIChatRoomInformationDTO.builder()
                .id(room.getId())
                .userId(room.getUserId())
                .title(displayTitle)
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    private AIChatResponseDTO mappingFromAIMessageToAIChatResponseDTO(String response, UUID chatRoomId, boolean isNewRoom) {
        return AIChatResponseDTO.builder()
                .chatRoomId(chatRoomId)
                .content(response)
                .isNewRoom(isNewRoom)
                .build();
    }

    private List<AIMessage.Attachment> processingMediaFile(List<MultipartFile> files) {
        List<AIMessage.Attachment> dbAttachments = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String fileName = generateFileName(file);
                String fileUrl = s3Service.uploadFile(file, "ai/file", fileName, true, MAX_IMG_SIZE);

                // Lưu vào list attachment để lưu database
                dbAttachments.add(new AIMessage.Attachment(fileUrl, file.getContentType()));
            }
        }
        return dbAttachments;
    }
}
