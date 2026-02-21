package org.ping_me.repository.jpa.reels;

import org.ping_me.model.reels.ReelSave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReelSaveRepository extends JpaRepository<ReelSave, Long> {
    Page<ReelSave> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByReelIdAndUserId(Long reelId, Long userId);

    void deleteAllByReelId(Long reelId);

    // efficient delete of a user's saved reel
    void deleteByReelIdAndUserId(Long reelId, Long userId);

    long countByReelId(Long id);
}
