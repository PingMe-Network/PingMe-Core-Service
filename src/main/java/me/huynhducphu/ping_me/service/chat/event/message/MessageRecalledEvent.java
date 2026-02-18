package me.huynhducphu.ping_me.service.chat.event.message;

import lombok.*;

/**
 * Admin 11/3/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageRecalledEvent {

    private String messageId;
    private Long roomId;

}
