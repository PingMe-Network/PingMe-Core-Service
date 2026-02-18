package me.huynhducphu.ping_me.service.chat.event.message;

import lombok.*;
import me.huynhducphu.ping_me.model.chat.Message;

/**
 * Admin 8/29/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageCreatedEvent {

    private Message message;

}
