package org.ping_me.service.chat;

import org.ping_me.dto.request.chat.message.MarkReadRequest;
import org.ping_me.dto.request.chat.message.SendMessageRequest;
import org.ping_me.dto.request.chat.message.SendWeatherMessageRequest;
import org.ping_me.dto.response.chat.message.HistoryMessageResponse;
import org.ping_me.dto.response.chat.message.MessageRecalledResponse;
import org.ping_me.dto.response.chat.message.MessageResponse;
import org.ping_me.dto.response.chat.message.ReadStateResponse;
import org.ping_me.model.User;
import org.ping_me.model.chat.Message;
import org.ping_me.model.chat.Room;
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
