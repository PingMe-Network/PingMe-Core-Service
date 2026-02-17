package me.huynhducphu.ping_me.service.chat.event.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin 2/17/2026
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageTypingEvent {

    boolean isTyping;

}
