package org.ping_me.service.reel.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.ping_me.dto.response.reels.ReelSearchHistoryResponse;
import org.ping_me.model.reels.ReelSearchHistory;
import org.ping_me.repository.jpa.auth.ReelSearchHistoryRepository;
import org.ping_me.service.reel.ReelSearchHistoryService;
import org.ping_me.service.user.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReelSearchHistoryServiceImpl implements ReelSearchHistoryService {


    ReelSearchHistoryRepository reelSearchHistoryRepository;
    CurrentUserProvider currentUserProvider;

    @Override
    public void recordSearch(String query, Integer resultCount) {
        try {
            var user = currentUserProvider.get();
            ReelSearchHistory h = ReelSearchHistory.builder()
                    .query(query)
                    .resultCount(resultCount)
                    .user(user)
                    .build();
            reelSearchHistoryRepository.save(h);
        } catch (Exception ignored) {
            // avoid breaking search if history saving fails
        }
    }

    @Override
    public Page<ReelSearchHistoryResponse> getMySearchHistory(Pageable pageable) {
        var user = currentUserProvider.get();
        var page = reelSearchHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return page.map(this::toDto);
    }

    @Override
    public void deleteById(Long id) {
        var user = currentUserProvider.get();
        var opt = reelSearchHistoryRepository.findById(id);
        if (opt.isPresent()) {
            var rec = opt.get();
            if (rec.getUser() != null && rec.getUser().getId().equals(user.getId())) {
                reelSearchHistoryRepository.deleteById(id);
            } else {
                throw new jakarta.persistence.EntityNotFoundException("Không tìm thấy lịch sử hoặc không có quyền xóa");
            }
        } else {
            throw new jakarta.persistence.EntityNotFoundException("Không tìm thấy lịch sử");
        }
    }

    @Override
    @Transactional
    public void deleteAllMyHistory() {
        var user = currentUserProvider.get();
        if (user == null || user.getId() == null) {
            throw new EntityNotFoundException("Người dùng không hợp lệ");
        }
        reelSearchHistoryRepository.deleteAllByUserId(user.getId());
    }

    private ReelSearchHistoryResponse toDto(ReelSearchHistory e) {
        var r = new ReelSearchHistoryResponse();
        r.setId(e.getId());
        r.setQuery(e.getQuery());
        r.setResultCount(e.getResultCount());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
