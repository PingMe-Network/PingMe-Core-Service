package org.ping_me.service.chat.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("isTyping")
    boolean isTyping;

}
