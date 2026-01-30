package me.huynhducphu.ping_me.repository.jpa.reels;

import me.huynhducphu.ping_me.model.constant.ReactionType;
import me.huynhducphu.ping_me.model.reels.ReelCommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReelCommentReactionRepository extends JpaRepository<ReelCommentReaction, Long> {
    Optional<ReelCommentReaction> findByCommentIdAndUserId(Long commentId, Long userId);

    long countByCommentId(Long commentId);

    long countByCommentIdAndType(Long commentId, ReactionType type);

    void deleteAllByCommentReelId(Long reelId);

    void deleteAllByCommentIdIn(List<Long> commentIds);

    void deleteAllByCommentId(Long commentId);
}
