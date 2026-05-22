package org.ping_me.dto.response.chat.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class JoinGroupByLinkResponse {
    private Boolean approvedImmediately;
    private String message;
    private RoomResponse room;
    private GroupJoinRequestResponse joinRequest;
}
