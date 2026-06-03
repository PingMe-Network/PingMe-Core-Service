package org.ping_me.dto.response.chat.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoteResponse {
    private Long id;
    private String messageId;
    private Long roomId;
    private Long createdByUserId;
    private String title;
    private String body;
}
