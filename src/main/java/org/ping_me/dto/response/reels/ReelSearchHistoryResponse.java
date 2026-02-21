package org.ping_me.dto.response.reels;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReelSearchHistoryResponse {
    private Long id;
    private String query;
    private Integer resultCount;
    private LocalDateTime createdAt;
}

