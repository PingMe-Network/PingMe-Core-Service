package me.huynhducphu.ping_me.service.chat.event.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
