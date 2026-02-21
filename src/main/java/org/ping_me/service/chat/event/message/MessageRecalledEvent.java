package org.ping_me.service.chat.event.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
