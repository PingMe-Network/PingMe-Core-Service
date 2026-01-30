package me.huynhducphu.ping_me.service.chat.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin 11/3/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageRecalledEvent {

    private String messageId;
    private Long roomId;

}
