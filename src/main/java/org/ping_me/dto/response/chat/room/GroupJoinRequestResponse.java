package org.ping_me.dto.response.chat.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ping_me.model.constant.GroupJoinRequestStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GroupJoinRequestResponse {
    private Long id;
    private Long roomId;
    private Long requesterId;
    private String requesterName;
    private String requesterAvatarUrl;
    private GroupJoinRequestStatus status;
    private Long reviewedByUserId;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
