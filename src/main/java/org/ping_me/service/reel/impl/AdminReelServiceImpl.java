package org.ping_me.service.reel.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.ping_me.config.s3.S3Service;
import org.ping_me.dto.request.reels.AdminReelFilterRequest;
import org.ping_me.dto.response.reels.AdminReelResponse;
import org.ping_me.model.constant.ReelStatus;
import org.ping_me.model.reels.Reel;
import org.huynhducphu.ping_me.repository.jpa.reels.*;
import org.ping_me.repository.jpa.reels.*;
import org.ping_me.repository.jpa.reels.spec.ReelSpecifications;
import org.ping_me.service.reel.AdminReelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminReelServiceImpl implements AdminReelService {

    // Repository
    ReelRepository reelRepository;
    ReelLikeRepository reelLikeRepository;
    ReelSaveRepository reelSaveRepository;
    ReelCommentRepository reelCommentRepository;
    ReelCommentReactionRepository reactionRepository;

    // Service
    S3Service s3Service;

    @Override
    public Page<AdminReelResponse> getReels(AdminReelFilterRequest filter, Pageable pageable) {
        return reelRepository.findAll(ReelSpecifications.byFilter(filter), pageable)
                .map(this::toAdminResponse);
    }

    @Override
    public AdminReelResponse getDetail(Long reelId) {
        var reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Reel"));
        return toAdminResponse(reel);
    }

    @Override
    public AdminReelResponse updateCaption(Long reelId, String caption) {
        var reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Reel"));

        reel.setCaption(caption);
        var saved = reelRepository.save(reel);
        return toAdminResponse(saved);
    }

    @Override
    public AdminReelResponse hideReel(Long reelId, String adminNote) {
        var reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Reel"));
        reel.setStatus(ReelStatus.HIDDEN);
        reel.setAdminNote(adminNote);
        var saved = reelRepository.save(reel);
        return toAdminResponse(saved);
    }

    @Override
    public void hardDelete(Long reelId) {
        var reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Reel"));

        reactionRepository.deleteAllByCommentReelId(reelId);
        reelCommentRepository.deleteAllByReelId(reelId);
        reelLikeRepository.deleteAllByReelId(reelId);
        reelSaveRepository.deleteAllByReelId(reelId);

        if (reel.getVideoUrl() != null) {
            s3Service.deleteFileByUrl(reel.getVideoUrl());
        }

        reelRepository.delete(reel);
    }

    private AdminReelResponse toAdminResponse(Reel reel) {
        AdminReelResponse res = new AdminReelResponse();

        res.setId(reel.getId());
        res.setVideoUrl(reel.getVideoUrl());
        res.setCaption(reel.getCaption());

        long likeCount = reelLikeRepository.countByReelId(reel.getId());
        long commentCount = reelCommentRepository.countByReelId(reel.getId());
        long saveCount = reelSaveRepository.countByReelId(reel.getId());

        res.setViewCount(reel.getViewCount());
        res.setLikeCount(likeCount);
        res.setCommentCount(commentCount);
        res.setSaveCount(saveCount);

        res.setUserId(reel.getUser().getId());
        res.setUserName(reel.getUser().getName());
        res.setUserAvatarUrl(reel.getUser().getAvatarUrl());

        res.setStatus(reel.getStatus().name());
        res.setAdminNote(reel.getAdminNote());

        res.setCreatedAt(reel.getCreatedAt());

        return res;
    }
}
