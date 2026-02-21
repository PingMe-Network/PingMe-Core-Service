package org.ping_me.repository.jpa.reels;

import org.ping_me.model.reels.ReelLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReelLikeRepository extends JpaRepository<ReelLike, Long> {
    boolean existsByReelIdAndUserId(Long reelId, Long userId);

    long countByReelId(Long reelId);

    void deleteByReelIdAndUserId(Long reelId, Long userId);

    void deleteAllByReelId(Long reelId);

    Page<ReelLike> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
