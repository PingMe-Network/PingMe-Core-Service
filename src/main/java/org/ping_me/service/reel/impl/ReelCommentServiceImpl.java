package org.ping_me.service.reel.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;
import org.ping_me.dto.request.reels.UpsertReelCommentRequest;
import org.ping_me.dto.response.reels.ReelCommentResponse;
import org.ping_me.model.User;
import org.ping_me.model.constant.ReactionType;
import org.ping_me.model.reels.ReelComment;
import org.ping_me.model.reels.ReelCommentReaction;
import org.ping_me.repository.jpa.reels.ReelCommentReactionRepository;
import org.ping_me.repository.jpa.reels.ReelCommentRepository;
import org.ping_me.repository.jpa.reels.ReelRepository;
import org.ping_me.service.reel.ReelCommentService;
import org.ping_me.service.user.CurrentUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReelCommentServiceImpl implements ReelCommentService {

    // Repository
    ReelRepository reelRepository;
    ReelCommentRepository reelCommentRepository;
    ReelCommentReactionRepository reactionRepository;

    // Provider
    CurrentUserProvider currentUserProvider;

    @Override
    public ReelCommentResponse createComment(Long reelId, UpsertReelCommentRequest dto) {
        var user = currentUserProvider.get();
        var reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Reel"));

        ReelComment parent = null;
        if (dto.getParentId() != null) {
            parent = reelCommentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy comment cha"));
            if (!parent.getReel().getId().equals(reelId)) {
                throw new IllegalArgumentException("Comment cha không thuộc Reel này");
            }
        }

        var comment = new ReelComment(dto.getContent(), reel, user);
        comment.setParent(parent);
        var saved = reelCommentRepository.saveAndFlush(comment);
        return toResponse(saved);
    }

    @Override
    public ReelCommentResponse updateComment(Long commentId, UpsertReelCommentRequest dto) {
        var user = currentUserProvider.get();

        var comment = reelCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bình luận"));

        if (!comment.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("Bạn không có quyền sửa bình luận này");

        comment.setContent(dto.getContent());
        var saved = reelCommentRepository.save(comment);

        return toResponse(saved);
    }

    @Override
    public Page<@NonNull ReelCommentResponse> getComments(Long reelId, Pageable pageable) {
        return reelCommentRepository.findByReelIdOrderByCreatedAtDesc(reelId, pageable)
                .map(this::toResponse);
    }

    @Override
    public void deleteComment(Long commentId) {
        var user = currentUserProvider.get();

        var comment = reelCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bình luận"));

        boolean isCommentOwner = comment.getUser().getId().equals(user.getId());
        boolean isReelOwner = comment.getReel().getUser().getId().equals(user.getId());

        if (!isCommentOwner && !isReelOwner) {
            throw new AccessDeniedException("Bạn không có quyền xóa bình luận này");
        }

        var replies = reelCommentRepository.findAllByParentId(commentId);

        java.util.List<Long> ids = new java.util.ArrayList<>();
        ids.add(commentId);
        ids.addAll(replies.stream().map(ReelComment::getId).toList());

        reactionRepository.deleteAllByCommentIdIn(ids);
        reelCommentRepository.deleteAllByParentId(commentId);
        reelCommentRepository.delete(comment);
    }


    private ReelCommentResponse toResponse(ReelComment reelComment) {

        User user = currentUserProvider.get();
        boolean isOwner = reelComment
                .getUser()
                .getId()
                .equals(reelComment.getReel().getUser().getId());
        long totalReactions = reactionRepository.countByCommentId(reelComment.getId());

        Map<String, Long> summary = new HashMap<>();
        for (var t : ReactionType.values()) {
            long cnt = reactionRepository.countByCommentIdAndType(reelComment.getId(), t);
            if (cnt > 0) summary.put(t.name(), cnt);
        }

        ReelCommentResponse res = new ReelCommentResponse();
        var myReact = reactionRepository
                .findByCommentIdAndUserId(reelComment.getId(), user.getId())
                .map(x -> x.getType().name())
                .orElse(null);

        res.setId(reelComment.getId());
        res.setContent(reelComment.getContent());

        res.setReelId(reelComment.getReel().getId());
        res.setIsReelOwner(isOwner);

        res.setUserId(reelComment.getUser().getId());
        res.setUserName(reelComment.getUser().getName());
        res.setUserAvatarUrl(reelComment.getUser().getAvatarUrl());

        res.setCreatedAt(reelComment.getCreatedAt());
        res.setReactionCount(totalReactions);

        res.setReactionSummary(summary);
        res.setMyReaction(myReact);
        res.setIsPinned(reelComment.getIsPinned());
        res.setParentId(reelComment.getParent() != null ? reelComment.getParent().getId() : null);

        return res;
    }


    @Override
    public Page<@NonNull ReelCommentResponse> getReplies(Long commentId, Pageable pageable) {
        return reelCommentRepository.findByParentIdOrderByCreatedAtAsc(commentId, pageable)
                .map(this::toResponse);
    }

    @Override
    public ReelCommentResponse pinComment(Long commentId) {
        var user = currentUserProvider.get();

        var comment = reelCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bình luận"));

        if (!comment.getReel().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền ghim bình luận");
        }

        reelCommentRepository.findFirstByReelIdAndIsPinnedTrue(comment.getReel().getId())
                .ifPresent(oldPinned -> {
                    oldPinned.setIsPinned(false);
                    reelCommentRepository.save(oldPinned);
                });

        comment.setIsPinned(true);
        var saved = reelCommentRepository.save(comment);
        return toResponse(saved);
    }

    @Override
    public ReelCommentResponse unpinComment(Long commentId) {
        var user = currentUserProvider.get();

        var comment = reelCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bình luận"));

        if (!comment.getReel().getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền bỏ ghim bình luận");
        }

        comment.setIsPinned(false);
        var saved = reelCommentRepository.save(comment);
        return toResponse(saved);
    }

    @Override
    public ReelCommentResponse react(Long commentId, ReactionType type) {
        var user = currentUserProvider.get();

        var comment = reelCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bình luận"));

        var existing = reactionRepository.findByCommentIdAndUserId(commentId, user.getId());

        if (existing.isPresent()) {
            existing.get().setType(type);
            reactionRepository.save(existing.get());
        } else {
            reactionRepository.save(new ReelCommentReaction(comment, user, type));
        }

        return toResponse(comment);
    }

    @Override
    public ReelCommentResponse unreact(Long commentId) {
        var user = currentUserProvider.get();

        var existing = reactionRepository.findByCommentIdAndUserId(commentId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Bạn chưa thả cảm xúc"));

        reactionRepository.delete(existing);

        var comment = reelCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bình luận"));

        return toResponse(comment);
    }

    @Override
    public boolean isCommentOwner(Long commentId) {
        var me = currentUserProvider.get();
        ReelComment comment = reelCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bình luận"));
        return comment.getUser() != null && comment.getUser().getId().equals(me.getId());
    }

}
