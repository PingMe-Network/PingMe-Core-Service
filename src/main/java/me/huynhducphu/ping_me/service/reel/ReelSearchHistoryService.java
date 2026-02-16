package me.huynhducphu.ping_me.service.reel;

import me.huynhducphu.ping_me.dto.response.reels.ReelSearchHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReelSearchHistoryService {
    void recordSearch(String query, Integer resultCount);

    Page<ReelSearchHistoryResponse> getMySearchHistory(Pageable pageable);

    void deleteById(Long id);

    void deleteAllMyHistory();
}
