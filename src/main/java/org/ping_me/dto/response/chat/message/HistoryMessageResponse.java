package org.ping_me.dto.response.chat.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Admin 8/31/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HistoryMessageResponse {

    private List<MessageResponse> messageResponses;
    private Boolean hasMore;
    private String nextBeforeId;
}
