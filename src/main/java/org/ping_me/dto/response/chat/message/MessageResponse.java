package org.ping_me.dto.response.chat.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ping_me.model.constant.MessageType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Admin 8/26/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageResponse {
    private String id;

    private Long roomId;
    private String clientMsgId;

    private Long senderId;

    private String content;
    private MessageType type;
    private String fileFormat;
    private List<String> mediaUrls;
    private LocalDateTime createdAt;
    private Boolean isEdited;
    private LocalDateTime editedAt;
    private Boolean isPinned;
    private LocalDateTime pinnedAt;
    private Long pinnedByUserId;

    private Boolean isActive;
    private Boolean isForwarded;
    private ForwardMetadataResponse forwardMetadata;
    private RepliedMessageResponse repliedMessage;
    private PollResponse poll;
    private NoteResponse note;
    private ReminderResponse reminder;
}
