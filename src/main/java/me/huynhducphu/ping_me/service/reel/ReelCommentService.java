package me.huynhducphu.ping_me.service.reel;

import me.huynhducphu.ping_me.dto.request.reels.UpsertReelCommentRequest;
import me.huynhducphu.ping_me.dto.response.reels.ReelCommentResponse;
import me.huynhducphu.ping_me.model.constant.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReelCommentService {
    ReelCommentResponse createComment(Long reelId, UpsertReelCommentRequest dto);

    ReelCommentResponse updateComment(Long commentId, UpsertReelCommentRequest dto);

    Page<ReelCommentResponse> getComments(Long reelId, Pageable pageable);

    void deleteComment(Long commentId);

    Page<ReelCommentResponse> getReplies(Long commentId, Pageable pageable);

    ReelCommentResponse pinComment(Long commentId);

    ReelCommentResponse unpinComment(Long commentId);

    ReelCommentResponse react(Long commentId, ReactionType type);

    ReelCommentResponse unreact(Long commentId);

    boolean isCommentOwner(Long commentId);
}
