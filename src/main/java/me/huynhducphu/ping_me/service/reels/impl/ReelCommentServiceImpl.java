package me.huynhducphu.ping_me.service.reels.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.request.reels.UpsertReelCommentRequest;
import me.huynhducphu.ping_me.dto.response.reels.ReelCommentResponse;
import me.huynhducphu.ping_me.model.constant.ReactionType;
import me.huynhducphu.ping_me.model.reels.ReelComment;
import me.huynhducphu.ping_me.model.reels.ReelCommentReaction;
import me.huynhducphu.ping_me.repository.jpa.reels.ReelCommentReactionRepository;
import me.huynhducphu.ping_me.repository.jpa.reels.ReelCommentRepository;
import me.huynhducphu.ping_me.repository.jpa.reels.ReelRepository;
import me.huynhducphu.ping_me.service.reels.ReelCommentService;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReelCommentServiceImpl implements ReelCommentService {

    private final ReelRepository reelRepository;
    private final ReelCommentRepository reelCommentRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ModelMapper modelMapper;
    private final ReelCommentReactionRepository reactionRepository;

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
    public Page<ReelCommentResponse> getComments(Long reelId, Pageable pageable) {
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


    private ReelCommentResponse toResponse(ReelComment c) {
        var me = currentUserProvider.get();

        ReelCommentResponse res = modelMapper.map(c, ReelCommentResponse.class);
        res.setReelId(c.getReel().getId());
        res.setUserId(c.getUser().getId());
        res.setUserName(c.getUser().getName());
        res.setUserAvatarUrl(c.getUser().getAvatarUrl());

        res.setIsPinned(c.getIsPinned());
        res.setParentId(c.getParent() != null ? c.getParent().getId() : null);

        long totalReactions = reactionRepository.countByCommentId(c.getId());
        res.setReactionCount(totalReactions);

        java.util.Map<String, Long> summary = new java.util.HashMap<>();
        for (var t : ReactionType.values()) {
            long cnt = reactionRepository.countByCommentIdAndType(c.getId(), t);
            if (cnt > 0) summary.put(t.name(), cnt);
        }
        res.setReactionSummary(summary);

        var myReact = reactionRepository.findByCommentIdAndUserId(c.getId(), me.getId());
        res.setMyReaction(myReact.map(r -> r.getType().name()).orElse(null));

        boolean isOwner = c.getUser().getId().equals(c.getReel().getUser().getId());
        res.setIsReelOwner(isOwner);
        return res;
    }


    @Override
    public Page<ReelCommentResponse> getReplies(Long commentId, Pageable pageable) {
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
