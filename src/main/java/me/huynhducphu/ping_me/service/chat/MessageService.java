package me.huynhducphu.ping_me.service.chat;

import me.huynhducphu.ping_me.dto.request.chat.message.MarkReadRequest;
import me.huynhducphu.ping_me.dto.request.chat.message.SendMessageRequest;
import me.huynhducphu.ping_me.dto.request.chat.message.SendWeatherMessageRequest;
import me.huynhducphu.ping_me.dto.response.chat.message.HistoryMessageResponse;
import me.huynhducphu.ping_me.dto.response.chat.message.MessageRecalledResponse;
import me.huynhducphu.ping_me.dto.response.chat.message.MessageResponse;
import me.huynhducphu.ping_me.dto.response.chat.message.ReadStateResponse;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.chat.Message;
import me.huynhducphu.ping_me.model.chat.Room;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin 8/26/2025
 *
 **/
public interface MessageService {
    MessageResponse sendMessage(SendMessageRequest sendMessageRequest);

    MessageResponse sendFileMessage(
            SendMessageRequest sendMessageRequest,
            MultipartFile file
    );

    // Xử lý gửi tin nhắn dạng WEATHER (thời tiết).
    //
    // Quy trình thực hiện:
    // 1. Gọi WeatherService để lấy dữ liệu thời tiết theo tọa độ (lat, lon).
    // 2. Serialize dữ liệu thời tiết sang JSON và gán vào content của message.
    // 3. Tạo SendMessageRequest với type = WEATHER và tái sử dụng pipeline sendMessage().
    MessageResponse sendWeatherMessage(SendWeatherMessageRequest req);

    MessageRecalledResponse recallMessage(String messageId);

    ReadStateResponse markAsRead(MarkReadRequest markReadRequest);

    HistoryMessageResponse getHistoryMessages(
            Long roomId, String beforeId, Integer size
    );

    Message createSystemMessage(Room room, String content, User user);
}
