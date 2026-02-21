package org.ping_me.repository.jpa.reels;

import org.ping_me.model.reels.ReelComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReelCommentRepository extends JpaRepository<ReelComment, Long> {
    Page<ReelComment> findByReelIdOrderByCreatedAtDesc(Long reelId, Pageable pageable);

    long countByReelId(Long reelId);

    Page<ReelComment> findByParentIdOrderByCreatedAtAsc(Long parentId, Pageable pageable);

    Optional<ReelComment> findFirstByReelIdAndIsPinnedTrue(Long reelId);

    List<ReelComment> findAllByParentId(Long parentId);

    void deleteAllByParentId(Long parentId);

    void deleteAllByReelId(Long reelId);
}
