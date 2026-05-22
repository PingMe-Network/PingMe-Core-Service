package org.ping_me.service.chat.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.ping_me.config.s3.S3Service;
import org.ping_me.dto.event.UserChatEvent;
import org.ping_me.dto.request.chat.message.CreatePollMessageRequest;
import org.ping_me.dto.request.chat.message.ForwardMessageRequest;
import org.ping_me.dto.request.chat.message.ForwardMessagesRequest;
import org.ping_me.dto.request.chat.message.EditMessageRequest;
import org.ping_me.dto.request.chat.message.MarkReadRequest;
import org.ping_me.dto.request.chat.message.SendMessageRequest;
import org.ping_me.dto.request.chat.message.SendWeatherMessageRequest;
import org.ping_me.dto.request.chat.message.VotePollRequest;
import org.ping_me.dto.response.chat.message.DeletedMessageResponse;
import org.ping_me.dto.response.chat.message.GroupMessageSummaryResponse;
import org.ping_me.dto.response.chat.message.HistoryMessageResponse;
import org.ping_me.dto.response.chat.message.MessageRecalledResponse;
import org.ping_me.dto.response.chat.message.MessageResponse;
import org.ping_me.dto.response.chat.message.ReadStateResponse;
import org.ping_me.dto.response.weather.WeatherResponse;
import org.ping_me.model.User;
import org.ping_me.model.chat.DeletedMessage;
import org.ping_me.model.chat.GroupSettings;
import org.ping_me.model.chat.Message;
import org.ping_me.model.chat.Poll;
import org.ping_me.model.chat.PollOption;
import org.ping_me.model.chat.Room;
import org.ping_me.model.chat.RoomParticipant;
import org.ping_me.model.common.DeletedMessageId;
import org.ping_me.model.common.RoomMemberId;
import org.ping_me.model.constant.MessageType;
import org.ping_me.model.constant.RoomRole;
import org.ping_me.model.constant.RoomType;
import org.ping_me.repository.jpa.chat.DeletedMessageRepository;
import org.ping_me.repository.jpa.chat.GroupSettingsRepository;
import org.ping_me.repository.jpa.chat.RoomParticipantRepository;
import org.ping_me.repository.jpa.chat.RoomRepository;
import org.ping_me.repository.mongodb.chat.MessageRepository;
import org.ping_me.utils.AIChatHelper;
import org.ping_me.service.chat.MessageCachingService;
import org.ping_me.service.chat.MessageService;
import org.ping_me.service.chat.event.message.MessageCreatedEvent;
import org.ping_me.service.chat.event.message.MessageRecalledEvent;
import org.ping_me.service.chat.event.message.MessageUpdatedEvent;
import org.ping_me.service.chat.event.room.RoomUpdatedEvent;
import org.ping_me.service.user.CurrentUserProvider;
import org.ping_me.service.weather.WeatherService;
import org.ping_me.utils.mapper.ChatMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin 8/26/2025
 *
 **/
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
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
    private final DeletedMessageRepository deletedMessageRepository;
    private final GroupSettingsRepository groupSettingsRepository;
    private final MessageRepository messageRepository;

    // PUBLISHER
    private final ApplicationEventPublisher eventPublisher;

    // UTILS
    private final ObjectMapper objectMapper;
    private final ChatMapper chatMapper;
    private final AIChatHelper aiChatHelper;

    @NonFinal
    @Value("${spring.kafka.topic.user-chat-dev}")
    String userChatTopic;

    @Qualifier("kafkaObjectTemplate")
    private final KafkaTemplate<String, Object> kafkaObjectTemplate;

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
    // Không sửa cấu trúc này

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

        if (sendMessageRequest.getType() == MessageType.POLL) {
            throw new IllegalArgumentException("Vui lòng dùng API tạo bình chọn");
        }

        // Nếu file này một dạng file thì
        // validate url hợp l
        if (sendMessageRequest.getType() == MessageType.IMAGE) {
            validateImageContent(sendMessageRequest.getContent());
        } else if (sendMessageRequest.getType() == MessageType.VIDEO
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
        requireCanSendMessage(room, roomParticipant);
        validateReplyMessage(sendMessageRequest.getRepliedMessageId(), roomId);

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
        message.setFileFormat(sendMessageRequest.getFileFormat());
        message.setRepliedMessageId(sendMessageRequest.getRepliedMessageId());
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

        try {
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

            // send chat event kafka
            publishUserChatAudit(currentUser, sendMessageRequest.getContent());

            return dto;
        } catch (RuntimeException ex) {
            // Compensation: nếu JPA rollback/exception thì xóa message đã lưu ở Mongo
            messageRepository.deleteById(message.getId());
            throw ex;
        }
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
            sendMessageRequest.setFileFormat(extractFileFormat(file));

            return sendMessage(sendMessageRequest);
        } catch (Exception ex) {
            if (url != null) s3Service.deleteFileByUrl(url);
            throw ex;
        }
    }

    @Override
    public MessageResponse sendImageBatchMessage(
            SendMessageRequest sendMessageRequest,
            List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Danh sách ảnh không được để trống");
        }

        if (sendMessageRequest.getType() != MessageType.IMAGE) {
            throw new IllegalArgumentException("Chỉ hỗ trợ nhiều ảnh cho tin nhắn IMAGE");
        }

        List<String> uploadedUrls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    throw new IllegalArgumentException("Ảnh không hợp lệ");
                }

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("Chỉ hỗ trợ upload nhiều ảnh");
                }

                String url = s3Service.uploadFile(
                        file,
                        "chats",
                        UUID.randomUUID().toString(),
                        true,
                        MAX_IMAGE_SIZE
                );
                uploadedUrls.add(url);
            }

            sendMessageRequest.setContent(objectMapper.writeValueAsString(uploadedUrls));
            sendMessageRequest.setFileFormat(files.size() > 1 ? "image-batch" : extractFileFormat(files.getFirst()));

            return sendMessage(sendMessageRequest);
        } catch (Exception ex) {
            uploadedUrls.forEach(s3Service::deleteFileByUrl);
            throw new RuntimeException(ex.getMessage(), ex);
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
        sendReq.setRepliedMessageId(null);

        // ============================
        // 4) Gọi lại pipeline chuẩn
        // ============================
        return sendMessage(sendReq);
    }

    @Override
    public MessageResponse createPollMessage(CreatePollMessageRequest request) {
        var currentUser = currentUserProvider.get();
        var senderId = currentUser.getId();
        var roomId = request.getRoomId();

        UUID clientMsgId = parseClientMsgId(request.getClientMsgId());

        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Phòng chat này không tồn tại"));
        RoomParticipant roomParticipant = validateRoomMember(roomId, senderId);
        requireCanCreatePoll(room, roomParticipant);
        validateReplyMessage(request.getRepliedMessageId(), roomId);

        validatePollRequest(request);

        var existed = messageRepository
                .findByRoomIdAndSenderIdAndClientMsgId(roomId, senderId, clientMsgId)
                .orElse(null);
        if (existed != null) return chatMapper.toMessageResponseDto(existed);

        Message message = new Message();
        message.setRoomId(roomId);
        message.setSenderId(senderId);
        message.setContent(request.getQuestion().trim());
        message.setType(MessageType.POLL);
        message.setPoll(buildPoll(request));
        message.setRepliedMessageId(request.getRepliedMessageId());
        message.setClientMsgId(clientMsgId);
        message.setCreatedAt(LocalDateTime.now());

        try {
            message = messageRepository.save(message);
        } catch (DataIntegrityViolationException ex) {
            message = messageRepository
                    .findByRoomIdAndSenderIdAndClientMsgId(roomId, senderId, clientMsgId)
                    .orElseThrow(() -> ex);
        }

        try {
            room.setLastMessageId(message.getId());
            room.setLastMessageAt(message.getCreatedAt());

            Message finalMsg = message;
            roomParticipant.setLastReadMessageId(finalMsg.getId());
            roomParticipant.setLastReadAt(finalMsg.getCreatedAt());

            eventPublisher.publishEvent(new MessageCreatedEvent(message));
            eventPublisher.publishEvent(new RoomUpdatedEvent(
                    room,
                    roomParticipantRepository.findByRoom_Id(room.getId()),
                    null
            ));

            var dto = chatMapper.toMessageResponseDto(message);
            if (cacheEnabled) {
                messageCachingService.cacheNewMessage(roomId, dto);
            }

            publishUserChatAudit(currentUser, request.getQuestion());
            return dto;
        } catch (RuntimeException ex) {
            messageRepository.deleteById(message.getId());
            throw ex;
        }
    }

    @Override
    public MessageResponse votePoll(String messageId, VotePollRequest request) {
        var currentUser = currentUserProvider.get();
        Long userId = currentUser.getId();

        Message message = messageRepository
                .findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn"));

        validateRoomMember(message.getRoomId(), userId);

        if (!message.isActive()) {
            throw new IllegalArgumentException("Không thể bình chọn tin nhắn đã thu hồi");
        }

        if (message.getType() != MessageType.POLL || message.getPoll() == null) {
            throw new IllegalArgumentException("Tin nhắn này không phải bình chọn");
        }

        Poll poll = message.getPoll();
        if (poll.getExpiresAt() != null && poll.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Bình chọn đã hết hạn");
        }

        List<String> selectedOptionIds = request.getOptionIds() == null
                ? List.of()
                : request.getOptionIds().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(optionId -> !optionId.isBlank())
                .distinct()
                .toList();

        if (!poll.allowMultiple() && selectedOptionIds.size() > 1) {
            throw new IllegalArgumentException("Bình chọn này chỉ cho phép chọn một lựa chọn");
        }

        Set<String> validOptionIds = poll.getOptions()
                .stream()
                .map(PollOption::getId)
                .collect(Collectors.toSet());

        for (String optionId : selectedOptionIds) {
            if (!validOptionIds.contains(optionId)) {
                throw new IllegalArgumentException("Lựa chọn bình chọn không hợp lệ");
            }
        }

        poll.getOptions().forEach(option -> {
            if (option.getVoterIds() == null) {
                option.setVoterIds(new HashSet<>());
            }
            option.getVoterIds().remove(userId);
            if (selectedOptionIds.contains(option.getId())) {
                option.getVoterIds().add(userId);
            }
        });

        message = messageRepository.save(message);
        return syncUpdatedMessage(message);
    }

    @Override
    public MessageResponse forwardMessage(ForwardMessageRequest request) {
        var currentUser = currentUserProvider.get();
        var senderId = currentUser.getId();
        var sourceMessage = validateForwardSourceMessage(request.getSourceMessageId(), senderId);
        UUID clientMsgId = parseClientMsgId(request.getClientMsgId());

        return forwardMessageToRoom(sourceMessage, senderId, request.getTargetRoomId(), clientMsgId);
    }

    @Override
    public List<MessageResponse> forwardMessages(ForwardMessagesRequest request) {
        var currentUser = currentUserProvider.get();
        var senderId = currentUser.getId();
        var sourceMessage = validateForwardSourceMessage(request.getSourceMessageId(), senderId);
        UUID clientMsgId = parseClientMsgId(request.getClientMsgId());

        var uniqueTargetRoomIds = request.getTargetRoomIds()
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (uniqueTargetRoomIds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách phòng đích không được để trống");
        }

        List<MessageResponse> responses = new ArrayList<>();
        for (Long targetRoomId : uniqueTargetRoomIds) {
            responses.add(forwardMessageToRoom(sourceMessage, senderId, targetRoomId, clientMsgId));
        }

        return responses;
    }

    @Override
    public DeletedMessageResponse deleteMessageForMe(String messageId) {
        var currentUser = currentUserProvider.get();

        Message message = messageRepository
                .findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn"));

        var memberId = new RoomMemberId(message.getRoomId(), currentUser.getId());
        var roomParticipant = roomParticipantRepository
                .findById(memberId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên của phòng chat này"));

        var deletedMessageId = new DeletedMessageId(
                message.getRoomId(),
                currentUser.getId(),
                messageId
        );

        if (!deletedMessageRepository.existsById(deletedMessageId)) {
            DeletedMessage deletedMessage = new DeletedMessage();
            deletedMessage.setId(deletedMessageId);
            deletedMessage.setRoom(roomParticipant.getRoom());
            deletedMessage.setUser(currentUser);
            deletedMessageRepository.save(deletedMessage);
        }

        if (messageId.equals(roomParticipant.getLastReadMessageId())) {
            roomParticipant.setLastReadMessageId(null);
            roomParticipant.setLastReadAt(null);
        }

        return new DeletedMessageResponse(messageId);
    }

    @Override
    public MessageResponse editMessage(String messageId, EditMessageRequest request) {
        var currentUser = currentUserProvider.get();

        Message messageToEdit = messageRepository
                .findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn"));

        if (!currentUser.getId().equals(messageToEdit.getSenderId())) {
            throw new AccessDeniedException("Không có quyền truy cập");
        }

        if (!messageToEdit.isActive()) {
            throw new IllegalArgumentException("Không thể chỉnh sửa tin nhắn đã thu hồi");
        }

        if (messageToEdit.getType() != MessageType.TEXT) {
            throw new IllegalArgumentException("Chỉ hỗ trợ chỉnh sửa tin nhắn văn bản");
        }

        long hours = ChronoUnit.HOURS.between(messageToEdit.getCreatedAt(), LocalDateTime.now());
        if (hours > 24) {
            throw new IllegalArgumentException("Bạn chỉ có thể chỉnh sửa tin nhắn trong vòng 24 giờ");
        }

        String newContent = request.getContent().trim();
        if (newContent.equals(messageToEdit.getContent())) {
            return chatMapper.toMessageResponseDto(messageToEdit);
        }

        messageToEdit.setContent(newContent);
        messageToEdit.setIsEdited(true);
        messageToEdit.setEditedAt(LocalDateTime.now());
        messageToEdit = messageRepository.save(messageToEdit);

        var dto = chatMapper.toMessageResponseDto(messageToEdit);
        if (cacheEnabled) {
            messageCachingService.updateMessage(messageToEdit.getRoomId(), messageId, dto);
        }

        eventPublisher.publishEvent(new MessageUpdatedEvent(messageToEdit));

        Room room = roomRepository
                .findById(messageToEdit.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Phòng chat này không tồn tại"));

        if (messageId.equals(room.getLastMessageId())) {
            var roomUpdatedEvent = new RoomUpdatedEvent(
                    room,
                    roomParticipantRepository.findByRoom_Id(room.getId()),
                    null
            );
            eventPublisher.publishEvent(roomUpdatedEvent);
        }

        return dto;
    }

    @Override
    public MessageResponse pinMessage(String messageId) {
        var currentUser = currentUserProvider.get();

        Message message = messageRepository
                .findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn"));

        validateRoomMember(message.getRoomId(), currentUser.getId());
        requireCanPinMessage(message.getRoomId(), currentUser.getId());

        if (!message.isActive()) {
            throw new IllegalArgumentException("Không thể ghim tin nhắn đã thu hồi");
        }

        if (message.getType() == MessageType.SYSTEM) {
            throw new IllegalArgumentException("Không thể ghim tin nhắn hệ thống");
        }

        if (message.isPinned()) {
            return chatMapper.toMessageResponseDto(message);
        }

        message.setIsPinned(true);
        message.setPinnedAt(LocalDateTime.now());
        message.setPinnedByUserId(currentUser.getId());
        message = messageRepository.save(message);

        return syncUpdatedMessage(message);
    }

    @Override
    public MessageResponse unpinMessage(String messageId) {
        var currentUser = currentUserProvider.get();

        Message message = messageRepository
                .findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn"));
        requireCanPinMessage(message.getRoomId(), currentUser.getId());

        if (!message.isPinned()) {
            return chatMapper.toMessageResponseDto(message);
        }

        message.setIsPinned(false);
        message.setPinnedAt(null);
        message.setPinnedByUserId(null);
        message = messageRepository.save(message);

        return syncUpdatedMessage(message);
    }

    @Override
    public List<MessageResponse> getPinnedMessages(Long roomId) {
        var currentUser = currentUserProvider.get();
        validateRoomMember(roomId, currentUser.getId());

        return messageRepository.findByRoomIdAndIsPinnedTrueOrderByPinnedAtDesc(roomId)
                .stream()
                .filter(Message::isActive)
                .map(chatMapper::toMessageResponseDto)
                .toList();
    }

    @Override
    public GroupMessageSummaryResponse summarizeLatestGroupMessages(Long roomId) {
        var currentUser = currentUserProvider.get();
        Long currentUserId = currentUser.getId();

        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Phòng chat này không tồn tại"));

        if (room.getRoomType() != RoomType.GROUP) {
            throw new IllegalArgumentException("Tính năng tóm tắt chỉ hỗ trợ phòng chat nhóm");
        }

        validateRoomMember(roomId, currentUserId);

        Set<String> deletedMessageIds = getDeletedMessageIds(roomId, currentUserId);
        List<Message> latestMessages = messageRepository
                .findByRoomIdOrderByIdDesc(roomId, PageRequest.of(0, 60))
                .stream()
                .filter(Message::isActive)
                .filter(message -> !deletedMessageIds.contains(message.getId()))
                .limit(20)
                .toList();

        if (latestMessages.isEmpty()) {
            return new GroupMessageSummaryResponse(
                    roomId,
                    0,
                    "Nhóm chưa có đủ nội dung để tóm tắt.",
                    LocalDateTime.now()
            );
        }

        Map<Long, String> participantNames = roomParticipantRepository
                .findByRoom_Id(roomId)
                .stream()
                .collect(Collectors.toMap(
                        participant -> participant.getUser().getId(),
                        participant -> participant.getUser().getName(),
                        (left, right) -> left
                ));

        List<Message> chronologicallySorted = new ArrayList<>(latestMessages);
        Collections.reverse(chronologicallySorted);

        StringBuilder conversation = new StringBuilder();
        for (Message message : chronologicallySorted) {
            String senderName = participantNames.getOrDefault(message.getSenderId(), "Thành viên");
            conversation
                    .append("- [")
                    .append(senderName)
                    .append("] ")
                    .append(formatSummaryMessageContent(message))
                    .append("\n");
        }

        String prompt = buildGroupSummaryPrompt(conversation.toString());
        String summary = aiChatHelper.useAi(prompt, List.of(), "gpt-4o-mini", 600);
        if (summary == null || summary.isBlank()) {
            summary = "Chưa thể tạo tóm tắt ở thời điểm hiện tại.";
        }

        return new GroupMessageSummaryResponse(
                roomId,
                chronologicallySorted.size(),
                summary.trim(),
                LocalDateTime.now()
        );
    }

    private void validatePollRequest(CreatePollMessageRequest request) {
        if (request.getOptions() == null || request.getOptions().size() < 2) {
            throw new IllegalArgumentException("Bình chọn cần ít nhất 2 lựa chọn");
        }

        if (request.getOptions().size() > 10) {
            throw new IllegalArgumentException("Bình chọn không được vượt quá 10 lựa chọn");
        }

        Set<String> normalizedOptions = request.getOptions()
                .stream()
                .map(option -> option == null ? "" : option.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        if (normalizedOptions.size() != request.getOptions().size()) {
            throw new IllegalArgumentException("Các lựa chọn bình chọn không được trùng nhau");
        }

        if (request.getExpiresAt() != null && !request.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hết hạn bình chọn phải nằm trong tương lai");
        }
    }

    private Poll buildPoll(CreatePollMessageRequest request) {
        List<PollOption> options = request.getOptions()
                .stream()
                .map(String::trim)
                .map(optionText -> new PollOption(
                        UUID.randomUUID().toString(),
                        optionText,
                        new HashSet<>()
                ))
                .toList();

        return new Poll(
                request.getQuestion().trim(),
                options,
                Boolean.TRUE.equals(request.getAllowMultiple()),
                request.getExpiresAt()
        );
    }

    private Message validateForwardSourceMessage(String sourceMessageId, Long senderId) {
        var sourceMessage = messageRepository
                .findById(sourceMessageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn nguồn"));

        if (!sourceMessage.isActive()) {
            throw new IllegalArgumentException("Không thể chuyển tiếp tin nhắn đã thu hồi");
        }

        if (sourceMessage.getType() == MessageType.SYSTEM) {
            throw new IllegalArgumentException("Không thể chuyển tiếp tin nhắn hệ thống");
        }

        if (sourceMessage.getType() == MessageType.POLL) {
            throw new IllegalArgumentException("Không thể chuyển tiếp bình chọn");
        }

        var sourceMemberId = new RoomMemberId(sourceMessage.getRoomId(), senderId);
        if (!roomParticipantRepository.existsById(sourceMemberId)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập tin nhắn nguồn");
        }

        return sourceMessage;
    }

    private RoomParticipant validateRoomMember(Long roomId, Long userId) {
        var memberId = new RoomMemberId(roomId, userId);
        return roomParticipantRepository
                .findById(memberId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên của phòng chat này"));
    }

    private void requireCanSendMessage(Room room, RoomParticipant participant) {
        if (room.getRoomType() != RoomType.GROUP || participant.getRole() != RoomRole.MEMBER) {
            return;
        }
        GroupSettings settings = groupSettingsRepository.findByRoomId(room.getId()).orElse(null);
        if (settings != null && !Boolean.TRUE.equals(settings.getAllowMemberSendMessage())) {
            throw new AccessDeniedException("Thành viên không được phép gửi tin nhắn trong nhóm này");
        }
    }

    private void requireCanCreatePoll(Room room, RoomParticipant participant) {
        if (room.getRoomType() != RoomType.GROUP || participant.getRole() != RoomRole.MEMBER) {
            return;
        }
        GroupSettings settings = groupSettingsRepository.findByRoomId(room.getId()).orElse(null);
        if (settings != null && !Boolean.TRUE.equals(settings.getAllowMemberCreatePoll())) {
            throw new AccessDeniedException("Thành viên không được phép tạo bình chọn trong nhóm này");
        }
    }

    private void requireCanPinMessage(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null || room.getRoomType() != RoomType.GROUP) {
            return;
        }
        RoomParticipant participant = validateRoomMember(roomId, userId);
        if (participant.getRole() != RoomRole.MEMBER) {
            return;
        }

        GroupSettings settings = groupSettingsRepository.findByRoomId(roomId).orElse(null);
        if (settings != null && !Boolean.TRUE.equals(settings.getAllowMemberPinMessage())) {
            throw new AccessDeniedException("Thành viên không được phép ghim tin nhắn trong nhóm này");
        }
    }

    private MessageResponse syncUpdatedMessage(Message message) {
        var dto = chatMapper.toMessageResponseDto(message);
        if (cacheEnabled) {
            messageCachingService.updateMessage(message.getRoomId(), message.getId(), dto);
        }

        eventPublisher.publishEvent(new MessageUpdatedEvent(message));
        return dto;
    }

    private UUID parseClientMsgId(String clientMsgIdRaw) {
        try {
            return UUID.fromString(clientMsgIdRaw);
        } catch (Exception exception) {
            throw new IllegalArgumentException("clientMsg không hợp lệ");
        }
    }

    private MessageResponse forwardMessageToRoom(
            Message sourceMessage,
            Long senderId,
            Long targetRoomId,
            UUID clientMsgId
    ) {
        Room targetRoom = roomRepository
                .findById(targetRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Phòng chat đích không tồn tại"));
        var targetMemberId = new RoomMemberId(targetRoomId, senderId);
        var targetParticipant = roomParticipantRepository
                .findById(targetMemberId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên của phòng chat đích"));

        var existed = messageRepository
                .findByRoomIdAndSenderIdAndClientMsgId(targetRoomId, senderId, clientMsgId)
                .orElse(null);
        if (existed != null) return chatMapper.toMessageResponseDto(existed);

        Message forwardedMessage = new Message();
        forwardedMessage.setRoomId(targetRoomId);
        forwardedMessage.setSenderId(senderId);
        forwardedMessage.setContent(sourceMessage.getContent());
        forwardedMessage.setType(sourceMessage.getType());
        forwardedMessage.setFileFormat(sourceMessage.getFileFormat());
        forwardedMessage.setClientMsgId(clientMsgId);
        forwardedMessage.setCreatedAt(LocalDateTime.now());
        forwardedMessage.setIsForwarded(true);
        forwardedMessage.setForwardedFromMessageId(sourceMessage.getId());
        forwardedMessage.setForwardedFromRoomId(sourceMessage.getRoomId());
        forwardedMessage.setForwardedFromSenderId(sourceMessage.getSenderId());

        try {
            forwardedMessage = messageRepository.save(forwardedMessage);
        } catch (DataIntegrityViolationException ex) {
            forwardedMessage = messageRepository
                    .findByRoomIdAndSenderIdAndClientMsgId(targetRoomId, senderId, clientMsgId)
                    .orElseThrow(() -> ex);
        }

        try {
            targetRoom.setLastMessageId(forwardedMessage.getId());
            targetRoom.setLastMessageAt(forwardedMessage.getCreatedAt());

            targetParticipant.setLastReadMessageId(forwardedMessage.getId());
            targetParticipant.setLastReadAt(forwardedMessage.getCreatedAt());

            var messageCreatedEvent = new MessageCreatedEvent(forwardedMessage);
            var roomUpdatedEvent = new RoomUpdatedEvent(
                    targetRoom,
                    roomParticipantRepository.findByRoom_Id(targetRoom.getId()),
                    null
            );

            eventPublisher.publishEvent(messageCreatedEvent);
            eventPublisher.publishEvent(roomUpdatedEvent);

            var dto = chatMapper.toMessageResponseDto(forwardedMessage);

            if (cacheEnabled) {
                messageCachingService.cacheNewMessage(targetRoomId, dto);
            }

            return dto;
        } catch (RuntimeException ex) {
            messageRepository.deleteById(forwardedMessage.getId());
            throw ex;
        }
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
        messageToRecall.setIsPinned(false);
        messageToRecall.setPinnedAt(null);
        messageToRecall.setPinnedByUserId(null);

        deleteMessageMediaSafely(messageToRecall);

        // Xóa content
        messageToRecall.setContent("");

        // Mongo document does not use JPA dirty checking, so persist the recall explicitly.
        messageRepository.save(messageToRecall);

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

    private void deleteMessageMediaSafely(Message message) {
        if (message.getType() == MessageType.TEXT
                || message.getType() == MessageType.SYSTEM
                || message.getType() == MessageType.WEATHER
                || message.getType() == MessageType.POLL) {
            return;
        }

        String content = message.getContent();
        if (content == null || content.isBlank()) {
            return;
        }

        if (message.getType() == MessageType.IMAGE) {
            for (String mediaUrl : extractMediaUrls(content)) {
                tryDeleteFileByUrl(mediaUrl, message.getId());
            }
            return;
        }

        tryDeleteFileByUrl(content, message.getId());
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
        validateHistoryRequest(roomId, size);

        var currentUser = currentUserProvider.get();
        var memberId = new RoomMemberId(roomId, currentUser.getId());
        if (!roomParticipantRepository.existsById(memberId))
            throw new RuntimeException("Not a room member");

        int fixed = Math.max(1, Math.min(size, 20));
        var deletedMessageIds = getDeletedMessageIds(roomId, currentUser.getId());
        boolean hasDeletedMessages = !deletedMessageIds.isEmpty();

        if (!hasDeletedMessages) {
            var cached = loadFromCache(roomId, beforeId, fixed);
            if (cached != null) return cached;
        }

        var db = loadFromDbCursor(roomId, beforeId, fixed, deletedMessageIds);
        if (!hasDeletedMessages) {
            cacheHistoryPage(roomId, beforeId, db);
        }

        return db;
    }


    private void validateHistoryRequest(Long roomId, Integer size) {
        if (roomId == null || size == null)
            throw new IllegalArgumentException("Invalid parameters");
    }

    private HistoryMessageResponse loadFromCache(Long roomId, String beforeId, int size) {
        if (!cacheEnabled) return null;

        var cached = messageCachingService.getMessages(roomId, beforeId, size);
        if (cached.isEmpty()) return null;

        String nextBeforeId = cached.getLast().getId();
        boolean hasMore = cached.size() == size;

        return new HistoryMessageResponse(cached, hasMore, nextBeforeId);
    }

    private void cacheHistoryPage(Long roomId, String beforeId, HistoryMessageResponse db) {
        if (!cacheEnabled || db.getMessageResponses().isEmpty()) return;

        if (beforeId == null)
            messageCachingService.cacheMessages(roomId, db.getMessageResponses());
        else
            messageCachingService.appendOlderMessages(roomId, db.getMessageResponses());
    }

    private HistoryMessageResponse loadFromDbCursor(
            Long roomId,
            String beforeId,
            int size,
            Set<String> deletedMessageIds
    ) {
        List<Message> selectedMessages = new ArrayList<>();
        String cursor = beforeId;
        boolean hasMore = false;

        while (selectedMessages.size() < size) {
            Pageable limit = PageRequest.of(0, size + 1);
            List<Message> fetched;

            if (cursor == null) {
                fetched = messageRepository.findByRoomIdOrderByIdDesc(roomId, limit);
            } else {
                fetched = messageRepository.findByRoomIdAndIdLessThanOrderByIdDesc(roomId, cursor, limit);
            }

            if (fetched.isEmpty()) {
                hasMore = false;
                break;
            }

            hasMore = fetched.size() > size;
            List<Message> trimmed = hasMore ? fetched.subList(0, size) : fetched;

            for (Message message : trimmed) {
                if (!deletedMessageIds.contains(message.getId())) {
                    selectedMessages.add(message);
                    if (selectedMessages.size() == size) break;
                }
            }

            if (!hasMore || trimmed.isEmpty()) {
                break;
            }

            cursor = trimmed.getLast().getId();
        }

        List<MessageResponse> responses = new ArrayList<>(
                selectedMessages.stream()
                        .limit(size)
                        .map(chatMapper::toMessageResponseDto)
                        .toList()
        );

        String nextBeforeId = null;
        if (!responses.isEmpty()) {
            nextBeforeId = responses.getLast().getId();
        }

        Collections.reverse(responses);

        return new HistoryMessageResponse(responses, hasMore, nextBeforeId);
    }

    private Set<String> getDeletedMessageIds(Long roomId, Long userId) {
        return deletedMessageRepository.findByIdRoomIdAndIdUserId(roomId, userId)
                .stream()
                .map(entry -> entry.getId().getMessageId())
                .collect(Collectors.toSet());
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

    private List<String> extractMediaUrls(String content) {
        String trimmed = content.trim();
        if (!trimmed.startsWith("[")) {
            return List.of(content);
        }

        try {
            List<String> urls = objectMapper.readValue(
                    trimmed,
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                    }
            );
            return urls == null ? List.of() : urls;
        } catch (Exception ex) {
            log.warn("Không thể parse danh sách media để thu hồi: {}", ex.getMessage());
            return List.of(content);
        }
    }

    private void tryDeleteFileByUrl(String url, String messageId) {
        if (url == null || url.isBlank()) {
            return;
        }

        try {
            s3Service.deleteFileByUrl(url);
        } catch (RuntimeException ex) {
            log.warn("Bỏ qua lỗi xóa media khi thu hồi message {}: {}", messageId, ex.getMessage());
        }
    }

    private void validateImageContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Dữ liệu phải là URL hợp lệ");
        }

        String trimmed = content.trim();
        if (!trimmed.startsWith("[")) {
            validateUrl(content);
            return;
        }

        try {
            List<String> urls = objectMapper.readValue(
                    trimmed,
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                    }
            );

            if (urls.isEmpty()) {
                throw new IllegalArgumentException("Dữ liệu phải là URL hợp lệ");
            }

            for (String url : urls) {
                validateUrl(url);
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Dữ liệu phải là URL hợp lệ");
        }
    }

    private void validateReplyMessage(String repliedMessageId, Long roomId) {
        if (repliedMessageId == null || repliedMessageId.isBlank()) {
            return;
        }

        Message repliedMessage = messageRepository
                .findById(repliedMessageId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tin nhắn được trả lời"));

        if (!Objects.equals(repliedMessage.getRoomId(), roomId)) {
            throw new IllegalArgumentException("Tin nhắn trả lời không thuộc phòng này");
        }
    }

    private static String extractFileFormat(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                return originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
            }
        }

        String contentType = file.getContentType();
        if (contentType != null) {
            int slashIndex = contentType.lastIndexOf('/');
            if (slashIndex >= 0 && slashIndex < contentType.length() - 1) {
                return contentType.substring(slashIndex + 1).toLowerCase(Locale.ROOT);
            }
        }

        return null;
    }

    private String buildGroupSummaryPrompt(String conversation) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý tóm tắt cuộc trò chuyện nhóm của ứng dụng PingMe.\n");
        sb.append("Hãy đọc 20 tin nhắn gần nhất và tóm tắt bằng tiếng Việt, ngắn gọn, dễ hiểu.\n");
        sb.append("Yêu cầu:\n");
        sb.append("1) 1 đoạn tóm tắt chính (tối đa 3 câu).\n");
        sb.append("2) 2-4 gạch đầu dòng về các điểm quan trọng hoặc việc cần chú ý.\n");
        sb.append("3) Không bịa thêm thông tin ngoài đoạn hội thoại.\n");
        sb.append("4) Không dùng markdown code block.\n\n");
        sb.append("Hội thoại:\n");
        sb.append(conversation);
        return sb.toString();
    }

    private String formatSummaryMessageContent(Message message) {
        if (message.getType() == MessageType.TEXT || message.getType() == MessageType.SYSTEM) {
            return message.getContent() == null || message.getContent().isBlank()
                    ? "(Tin nhắn trống)"
                    : message.getContent();
        }
        if (message.getType() == MessageType.IMAGE) {
            return "(Đã gửi hình ảnh)";
        }
        if (message.getType() == MessageType.VIDEO) {
            return "(Đã gửi video)";
        }
        if (message.getType() == MessageType.FILE) {
            return "(Đã gửi tệp đính kèm)";
        }
        if (message.getType() == MessageType.WEATHER) {
            return "(Đã gửi thông tin thời tiết)";
        }
        if (message.getType() == MessageType.POLL) {
            if (message.getPoll() != null && message.getPoll().getQuestion() != null) {
                return "Bình chọn: " + message.getPoll().getQuestion();
            }
            return "(Đã tạo bình chọn)";
        }
        return "(Tin nhắn không xác định)";
    }

    // Sửa lại tham số nhận vào là User thay vì chỉ senderId
    private void publishUserChatAudit(User sender, String message) {
        try {
            // Lưu ý: Nhớ update DTO UserChatEvent thêm trường senderName
            UserChatEvent event = new UserChatEvent(
                    sender.getId(),
                    sender.getName(),
                    message,
                    System.currentTimeMillis()
            );

            kafkaObjectTemplate.send(userChatTopic, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Kafka: Send event senderId {} ({}) send message {}", sender.getId(), sender.getName(), message);
                        } else {
                            log.error("Kafka: Send event failed: {}", ex.getMessage());
                        }
                    });

        } catch (Exception ex) {
            log.error("Error Kafka event: {}", ex.getMessage());
        }
    }
}
