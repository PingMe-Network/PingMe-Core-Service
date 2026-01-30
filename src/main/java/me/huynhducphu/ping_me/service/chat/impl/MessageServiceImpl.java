package me.huynhducphu.ping_me.service.chat.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.request.chat.message.MarkReadRequest;
import me.huynhducphu.ping_me.dto.request.chat.message.SendMessageRequest;
import me.huynhducphu.ping_me.dto.request.chat.message.SendWeatherMessageRequest;
import me.huynhducphu.ping_me.dto.response.chat.message.HistoryMessageResponse;
import me.huynhducphu.ping_me.dto.response.chat.message.MessageRecalledResponse;
import me.huynhducphu.ping_me.dto.response.chat.message.MessageResponse;
import me.huynhducphu.ping_me.dto.response.chat.message.ReadStateResponse;
import me.huynhducphu.ping_me.dto.response.weather.WeatherResponse;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.chat.Message;
import me.huynhducphu.ping_me.model.chat.Room;
import me.huynhducphu.ping_me.model.chat.RoomParticipant;
import me.huynhducphu.ping_me.model.common.RoomMemberId;
import me.huynhducphu.ping_me.model.constant.MessageType;
import me.huynhducphu.ping_me.repository.jpa.chat.RoomParticipantRepository;
import me.huynhducphu.ping_me.repository.jpa.chat.RoomRepository;
import me.huynhducphu.ping_me.repository.mongodb.chat.MessageRepository;
import me.huynhducphu.ping_me.service.chat.MessageCachingService;
import me.huynhducphu.ping_me.service.chat.MessageService;
import me.huynhducphu.ping_me.service.chat.event.MessageCreatedEvent;
import me.huynhducphu.ping_me.service.chat.event.MessageRecalledEvent;
import me.huynhducphu.ping_me.service.chat.event.RoomUpdatedEvent;
import me.huynhducphu.ping_me.service.s3.S3Service;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import me.huynhducphu.ping_me.service.weather.WeatherService;
import me.huynhducphu.ping_me.utils.ChatMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Admin 8/26/2025
 *
 **/
@Service
@Transactional
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024L;

    @Value("${app.messages.cache.enabled}")
    private boolean cacheEnabled;

    // SERVICE
    private final S3Service s3Service;
    private final MessageCachingService messageCachingService;
    private final WeatherService weatherService;

    // PROVIDER
    private final CurrentUserProvider currentUserProvider;

    // REPOSITORY
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final MessageRepository messageRepository;

    // PUBLISHER
    private final ApplicationEventPublisher eventPublisher;

    // UTILS
    private final ObjectMapper objectMapper;
    private final ChatMapper chatMapper;

    /* ========================================================================== */
    /*                         CACHING MESSAGE                                    */
    /* ========================================================================== */
    // Redis List Order:
    // index 0        -> newest message
    // index increase -> older messages
    //
    // API / FE Order:
    // index 0        -> oldest message
    // index increase -> newest messages
    //
    //
    // Kkhông sửa cấu trúc này

    /* ========================================================================== */
    /*                         CÁC HÀM XỬ LÝ GỬI TIN NHẮN                         */
    /* ========================================================================== */

    // Hàm chính xử lý tin nhắn
    //
    // Quy trình:
    // 1. Lấy thông tin người dùng hiện tại
    // 2. Kiểm tra phòng chat và quyền tham gia
    // 3. Kiểm tra clientMsgId để tránh trùng tin nhắn
    // 4. Tạo và lưu tin nhắn mới vào cơ sở dữ liệu
    // 5. Cập nhật trạng thái phòng và người dùng
    // 6. Phát sự kiện WebSocket thông báo tin nhắn mới
    @Override
    public MessageResponse sendMessage(SendMessageRequest sendMessageRequest) {
        // Lấy thộng tin người dùng hiện tại
        var currentUser = currentUserProvider.get();

        // Trích xuất ra thông tin người gửi
        // + Mã người dùng
        // + Mã phòng chat người đã gửi
        var senderId = currentUser.getId();
        var roomId = sendMessageRequest.getRoomId();

        // Nếu file này một dạng file thì
        // validate url hợp l
        if (sendMessageRequest.getType() == MessageType.IMAGE
                || sendMessageRequest.getType() == MessageType.VIDEO
                || sendMessageRequest.getType() == MessageType.FILE) {
            validateUrl(sendMessageRequest.getContent());
        }

        // Kiểm tra clientMsgId có hợp lệ không
        // clientMsgId tránh người dùng spam khi
        // đường truyền không ổn định
        UUID clientMsgId;
        try {
            clientMsgId = UUID.fromString(sendMessageRequest.getClientMsgId());
        } catch (Exception exception) {
            throw new IllegalArgumentException("clientMsg không hợp lệ");
        }

        // Tìm phòng chat mà người dùng đã gửi tin nhắn
        // Nếu tìm không được trả về lỗi
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Phòng chat này không tồn tại"));
        var roomMemberId = new RoomMemberId(roomId, senderId);
        var roomParticipant = roomParticipantRepository
                .findById(roomMemberId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên của phòng chat này"));

        // Kiểm tra tin nhắn người dùng gửi đã tồn tại chưa, Kiểm tra bằng mã clientMsgId
        // Nếu tìm thấy thì trả về tin nhắn đã tồn tại trong cơ sở dữ liệu
        var existed = messageRepository
                .findByRoomIdAndSenderIdAndClientMsgId(roomId, senderId, clientMsgId)
                .orElse(null);
        if (existed != null) return chatMapper.toMessageResponseDto(existed);

        // Nếu chưa tồn tại, tạo tin nhắn mới
        Message message = new Message();
        message.setRoomId(roomId);
        message.setSenderId(senderId);
        message.setContent(sendMessageRequest.getContent());
        message.setType(sendMessageRequest.getType());
        message.setClientMsgId(clientMsgId);
        message.setCreatedAt(LocalDateTime.now());

        // Lưu tin nhắn vào cơ sở dữ liệu
        // Thực hiện Try Catch để tránh Race Condition
        // cho trường hợp User dùng 2 máy gửi tin nhắn cùng lúc
        try {
            message = messageRepository.save(message);
        } catch (DataIntegrityViolationException ex) {
            message = messageRepository
                    .findByRoomIdAndSenderIdAndClientMsgId(roomId, senderId, clientMsgId)
                    .orElseThrow(() -> ex);
        }

        // Cập nhật trạng thái phòng khi người dùng nhắn tin
        // Thông tin cập nhật bao gồm:
        // + Tin nhắn cuối cùng
        // + Thời gian nhắn tin nhắn cuối cùng
        room.setLastMessageId(message.getId());
        room.setLastMessageAt(message.getCreatedAt());

        // Cập nhật trạng thái của người dùng tham gia phòng (người dùng hiện tại)
        // Thông tin cập nhật bao gồm:
        // + Tin nhắn lần cuối đọc (seen)
        // + Thời gian đọc tin nhắn cuối cùng
        //
        // Dễ hiểu: người dùng A chat tin nhắn đó, thì mặc
        // định người dùng A đọc tất cả tin nhắn từ đầu đến tin nhắn
        // hiện tại của người dùng A
        Message finalMsg = message;
        roomParticipant.setLastReadMessageId(finalMsg.getId());
        roomParticipant.setLastReadAt(finalMsg.getCreatedAt());

        // --------------------------------------------------------------------------------
        // WEBSOCKET

        // Sự kiện MESSAGE_CREATED (tạo tin nhắn mới)
        var messageCreatedEvent = new MessageCreatedEvent(message);

        // Sử kiện ROOM_UPDATED (thông báo phòng có tin nhắn mới)
        var roomUpdatedEvent = new RoomUpdatedEvent(
                room,
                roomParticipantRepository.findByRoom_Id(room.getId()),
                null
        );

        // Bắn sự kiện Websocket
        eventPublisher.publishEvent(messageCreatedEvent);
        eventPublisher.publishEvent(roomUpdatedEvent);
        // --------------------------------------------------------------------------------

        var dto = chatMapper.toMessageResponseDto(message);

        // Caching Message
        if (cacheEnabled)
            messageCachingService.cacheNewMessage(roomId, dto);

        return dto;
    }

    // Hàm xử lý gửi tin nhắn dạng MEDIA (File, Video, Image)
    //
    // Quy trình thực hiện:
    // 1. Upload file media lên S3 (AWS)
    // 2. Nhận lại URL và gán vào content của message
    // 3. Gọi hàm sendMessage() để xử lý lưu tin nhắn
    //
    // Nếu quá trình gửi tin nhắn xảy ra lỗi:
    // + Xóa file vừa upload khỏi S3 để tránh rác
    // + Quăng lại exception để phía trên xử lý
    @Override
    public MessageResponse sendFileMessage(
            SendMessageRequest sendMessageRequest,
            MultipartFile file
    ) {
        if (file == null)
            throw new IllegalArgumentException("File không tồn tại");

        if (sendMessageRequest.getType() == MessageType.TEXT)
            throw new IllegalArgumentException("Tin nhắn dạng TEXT không được upload file");

        UUID fileName = UUID.randomUUID();

        String url = null;
        try {
            url = s3Service.uploadFile(
                    file,
                    "chats",
                    fileName.toString(),
                    true,
                    MAX_IMAGE_SIZE
            );
            sendMessageRequest.setContent(url);

            return sendMessage(sendMessageRequest);
        } catch (Exception ex) {
            if (url != null) s3Service.deleteFileByUrl(url);
            throw ex;
        }
    }


    // Xử lý gửi tin nhắn dạng WEATHER (thời tiết).
    //
    // Quy trình thực hiện:
    // 1. Gọi WeatherService để lấy dữ liệu thời tiết theo tọa độ (lat, lon).
    // 2. Serialize dữ liệu thời tiết sang JSON và gán vào content của message.
    // 3. Tạo SendMessageRequest với type = WEATHER và tái sử dụng pipeline sendMessage().
    @Override
    public MessageResponse sendWeatherMessage(SendWeatherMessageRequest req) {
        // ============================
        // 1) Lấy dữ liệu thời tiết
        // ============================
        WeatherResponse weather = weatherService.getWeather(req.getLat(), req.getLon());

        // ============================
        // 2) Serialize JSON vào content
        // ============================
        String contentJson;
        try {
            contentJson = objectMapper.writeValueAsString(weather);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể serialize dữ liệu thời tiết");
        }

        // ============================
        // 3) Tạo SendMessageRequest để tái sử dụng sendMessage()
        // ============================
        var sendReq = new SendMessageRequest();
        sendReq.setRoomId(req.getRoomId());
        sendReq.setContent(contentJson);
        sendReq.setType(MessageType.WEATHER);
        sendReq.setClientMsgId(req.getClientMsgId());

        // ============================
        // 4) Gọi lại pipeline chuẩn
        // ============================
        return sendMessage(sendReq);
    }


    /* ========================================================================== */
    /*                         CÁC HÀM XỬ LÝ THU HỒI TIN NHẮN                     */
    /* ========================================================================== */

    @Override
    public MessageRecalledResponse recallMessage(String messageId) {
        var currentUser = currentUserProvider.get();

        Message messageToRecall = messageRepository
                .findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn"));

        if (!currentUser.getId().equals(messageToRecall.getSenderId()))
            throw new AccessDeniedException("Không có quyền truy cập");

        long hours = ChronoUnit.HOURS.between(messageToRecall.getCreatedAt(), LocalDateTime.now());
        if (hours > 24)
            throw new IllegalArgumentException("Bạn chỉ có thể thu hồi tin nhắn trong vòng 24 giờ");

        // Disable message
        messageToRecall.setIsActive(false);

        // Không phải TEXT -> xóa file
        if (!messageToRecall.getType().equals(MessageType.TEXT)) {
            s3Service.deleteFileByUrl(messageToRecall.getContent());
        }

        // Xóa content
        messageToRecall.setContent("");

        // ---------------------------
        // UPDATE CACHE
        // ---------------------------
        Long roomId = messageToRecall.getRoomId();
        var dto = chatMapper.toMessageResponseDto(messageToRecall);

        if (cacheEnabled)
            messageCachingService.updateMessage(roomId, messageId, dto);


        // ---------------------------
        // WEBSOCKET EVENT
        // ---------------------------
        var messageRecalledEvent = new MessageRecalledEvent(messageId, roomId);
        eventPublisher.publishEvent(messageRecalledEvent);

        return new MessageRecalledResponse(messageId);
    }

    /* ========================================================================== */
    /*              CÁC HÀM XỬ LÝ NGƯỜI DÙNG ĐÃ XEM TIN NHẮN                     */
    /* ========================================================================== */

    @Override
    public ReadStateResponse markAsRead(MarkReadRequest markReadRequest) {
        var currentUser = currentUserProvider.get();

        Long userId = currentUser.getId();
        Long roomId = markReadRequest.getRoomId();
        String lastReadMessageId = markReadRequest.getLastReadMessageId();

        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Phòng chat này không tồn tại"));
        var roomMemberId = new RoomMemberId(roomId, userId);
        RoomParticipant roomParticipant = roomParticipantRepository
                .findById(roomMemberId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên của phòng chat này"));

        var msgOpt = messageRepository.findById(lastReadMessageId);
        if (msgOpt.isEmpty() || !msgOpt.get().getRoomId().equals(roomId))
            throw new IllegalArgumentException("tin nhắn không thuộc phòng này");

        Message lastReadMessage = msgOpt.get();
        String newPointer = lastReadMessageId;

        if (roomParticipant.getLastReadMessageId() != null) {
            Optional<Message> oldMsgOpt = messageRepository.findById(roomParticipant.getLastReadMessageId());
            if (oldMsgOpt.isPresent()) {
                Message oldMessage = oldMsgOpt.get();

                if (lastReadMessage.getCreatedAt().isBefore(oldMessage.getCreatedAt())) {
                    newPointer = roomParticipant.getLastReadMessageId();
                }
            }
        }

        roomParticipant.setLastReadMessageId(newPointer);
        roomParticipant.setLastReadAt(LocalDateTime.now());

        long unread = 0L;
        if (room.getLastMessageId() != null) {
            unread = messageRepository.countByRoomIdAndCreatedAtGreaterThan(
                    roomId,
                    lastReadMessage.getCreatedAt()
            );
        }

        return new ReadStateResponse(
                roomId,
                userId,
                newPointer,
                roomParticipant.getLastReadAt(),
                unread
        );
    }

    /* ========================================================================== */
    /*                  CÁC HÀM XỬ LÝ LẤY LỊCH SỬ TIN NHẮN                        */
    /* ========================================================================== */
    @Override
    public HistoryMessageResponse getHistoryMessages(
            Long roomId,
            String beforeId,
            Integer size
    ) {

        // --------------------------------------------------------------------------------
        // Validate input
        // --------------------------------------------------------------------------------
        if (roomId == null || size == null)
            throw new IllegalArgumentException("Invalid parameters");

        var currentUser = currentUserProvider.get();
        var memberId = new RoomMemberId(roomId, currentUser.getId());

        if (!roomParticipantRepository.existsById(memberId))
            throw new RuntimeException("Not a room member");

        int fixed = Math.max(1, Math.min(size, 20));
        // --------------------------------------------------------------------------------

        // =========================================================================================
        // Flow lấy lịch sử tin nhắn (3 bước):
        // 1) beforeId == null → lấy newest messages (ưu tiên cache, fallback DB)
        // 2) beforeId != null → cố lấy từ cache (older messages)
        // 3) Nếu cache rỗng → load thêm từ DB, rồi append vào cache
        // =========================================================================================


        // ----------------------------------------
        // Case 1: Lấy batch mới nhất (beforeId == null)
        // Ưu tiên đọc cache; nếu cache trống → đọc DB và cache lại.
        // ----------------------------------------
        if (beforeId == null) {

            // -----------------------------
            // 1. Ưu tiên đọc cache
            // -----------------------------
            if (cacheEnabled) {
                var cached = messageCachingService.getMessages(roomId, null, fixed);
                if (!cached.isEmpty()) {
                    String nextBeforeId = cached.getLast().getId();
                    return new HistoryMessageResponse(cached, true, nextBeforeId);
                }
            }

            // -----------------------------
            // 2. Fallback DB
            // -----------------------------
            var db = loadFromDbCursor(roomId, null, fixed);

            // -----------------------------
            // 3. Cache lại (nếu bật cache)
            // -----------------------------
            if (cacheEnabled && !db.getMessageResponses().isEmpty()) {
                messageCachingService.cacheMessages(roomId, db.getMessageResponses());
            }

            return db;
        }


        // ----------------------------------------
        // Case 2: beforeId != null → cố lấy older messages từ cache
        // Nếu cache có → trả luôn, tránh hit DB
        // ----------------------------------------
        if (cacheEnabled) {
            var older = messageCachingService.getMessages(roomId, beforeId, fixed);

            if (!older.isEmpty()) {
                String nextBeforeId = older.getLast().getId();
                return new HistoryMessageResponse(older, true, nextBeforeId);
            }
        }


        // ----------------------------------------
        // Case 3: Cache không có → fallback DB
        // Sau khi load từ DB thì append vào cache
        // ----------------------------------------
        var db = loadFromDbCursor(roomId, beforeId, fixed);

        if (cacheEnabled && !db.getMessageResponses().isEmpty())
            messageCachingService.appendOlderMessages(roomId, db.getMessageResponses());

        return db;
    }

    private HistoryMessageResponse loadFromDbCursor(
            Long roomId,
            String beforeId,
            int size
    ) {
        // Query size + 1 để biết còn trang sau không
        Pageable limit = PageRequest.of(0, size + 1);
        List<Message> res;

        if (beforeId == null)
            res = messageRepository
                    .findByRoomIdOrderByIdDesc(roomId, limit);
        else
            res = messageRepository
                    .findByRoomIdAndIdLessThanOrderByIdDesc(roomId, beforeId, limit);

        // Kiểm tra hasMore và Cắt bớt phần tử thừa (QUAN TRỌNG)
        boolean hasMore = res.size() > size;
        List<Message> trimmedMessages = hasMore ? res.subList(0, size) : res;

        // Convert sang DTO
        List<MessageResponse> responses = new ArrayList<>(
                trimmedMessages.stream().map(chatMapper::toMessageResponseDto).toList()
        );

        // Tính nextBeforeId dựa trên tin nhắn CŨ NHẤT (tin cuối cùng của list giảm dần)
        String nextBeforeId = null;
        if (!responses.isEmpty())
            nextBeforeId = responses.getLast().getId();


        // Đảo ngược lại thành Tăng Dần (Cũ -> Mới) để khớp với UI Chat
        Collections.reverse(responses);

        return new HistoryMessageResponse(responses, hasMore, nextBeforeId);
    }

    /* ========================================================================== */
    /*                  TẠO TIN NHẮN HỆ THỐNG                                     */
    /* ========================================================================== */
    @Override
    public Message createSystemMessage(Room room, String content, User user) {
        var msg = new Message();
        msg.setRoomId(room.getId());
        msg.setSenderId(user.getId());
        msg.setType(MessageType.SYSTEM);
        msg.setContent(content);
        msg.setIsActive(true);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setClientMsgId(UUID.randomUUID());


        var saved = messageRepository.save(msg);
        var dto = chatMapper.toMessageResponseDto(saved);

        if (cacheEnabled)
            messageCachingService.cacheNewMessage(room.getId(), dto);


        return saved;
    }


    /* ========================================================================== */
    /*                           CÁC HÀM HỖ TRỢ KHÁC                              */
    /* ========================================================================== */
    private static void validateUrl(String url) {
        try {
            URI u = URI.create(url);
            if (u.getScheme() == null || u.getHost() == null)
                throw new IllegalArgumentException("Dữ liệu phải là URL hợp lệ");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Dữ liệu phải là URL hợp lệ");
        }
    }

}
