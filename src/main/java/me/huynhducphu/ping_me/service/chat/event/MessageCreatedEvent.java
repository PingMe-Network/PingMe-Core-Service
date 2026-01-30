package me.huynhducphu.ping_me.service.chat.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.huynhducphu.ping_me.model.chat.Message;

/**
 * Admin 8/29/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageCreatedEvent {

    private Message message;

}
