package me.huynhducphu.ping_me.service.reels.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.request.reels.ReelRequest;
import me.huynhducphu.ping_me.dto.response.reels.ReelResponse;
import me.huynhducphu.ping_me.model.reels.Reel;
import me.huynhducphu.ping_me.model.reels.ReelLike;
import me.huynhducphu.ping_me.model.reels.ReelSave;
import me.huynhducphu.ping_me.model.reels.ReelView;
import me.huynhducphu.ping_me.repository.jpa.reels.*;
import me.huynhducphu.ping_me.service.reels.ReelService;
import me.huynhducphu.ping_me.service.s3.S3Service;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReelServiceImpl implements ReelService {

    @Value("${app.reels.max-video-size}")
    private DataSize maxReelVideoSize;

    @Value("${app.reels.folder}")
    private String reelsFolder;

    private final ReelRepository reelRepository;
    private final ReelLikeRepository reelLikeRepository;
    private final ReelSaveRepository reelSaveRepository;
    private final ReelViewRepository reelViewRepository;
    private final ReelCommentRepository reelCommentRepository;

    private final CurrentUserProvider currentUserProvider;
    private final S3Service s3Service;
    private final ModelMapper modelMapper;
    private final ReelCommentReactionRepository reactionRepository;
    private final me.huynhducphu.ping_me.service.reels.ReelSearchHistoryService reelSearchHistoryService;

    @Override
    public ReelResponse createReel(ReelRequest dto, MultipartFile video) {
        var user = currentUserProvider.get();

        if (video == null || video.isEmpty())
            throw new IllegalArgumentException("Video không được bỏ trống");

        String contentType = video.getContentType();
        if (contentType == null || !contentType.startsWith("video/"))
            throw new IllegalArgumentException("File upload phải là video");

        String original = video.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf("."))
                : ".mp4";

        String randomFileName = UUID.randomUUID() + ext;
        long maxBytes = maxReelVideoSize.toBytes();

        String url = s3Service.uploadFile(
                video, reelsFolder, randomFileName, true, maxBytes
        );

        // normalize hashtags list: remove leading '#', trim, filter empties
        List<String> normalized = null;
        if (dto.getHashtags() != null) {
            normalized = dto.getHashtags().stream()
                    .filter(h -> h != null && !h.isBlank())
                    .map(String::trim)
                    .map(h -> h.startsWith("#") ? h.substring(1) : h)
                    .map(String::toLowerCase)
                    .distinct()
                    .collect(Collectors.toList());
        }

        var reel = new Reel(url, dto.getCaption(), normalized);
        reel.setUser(user);

        var saved = reelRepository.saveAndFlush(reel);
        return toReelResponse(saved, user.getId());
    }

    @Override
    public Page<ReelResponse> getFeed(Pageable pageable) {
        var me = currentUserProvider.get();

        return reelRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(reel -> toReelResponse(reel, me.getId()));
    }

    @Override
    public Page<ReelResponse> searchByTitle(String query, Pageable pageable) {
        var me = currentUserProvider.get();
        if (query == null || query.isBlank()) {
            return getFeed(pageable);
        }
        // handled below with hashtag-aware search
        String q = query.trim();
        Page<Reel> rawPage;
        if (q.startsWith("#")) {
            // normalize tag for search (strip leading '#')
            String tag = q.substring(1).toLowerCase();
            rawPage = reelRepository.searchByHashtag(tag, pageable);
        } else {
            rawPage = reelRepository.searchByTitle(q, pageable);
        }
        var page = rawPage.map(reel -> toReelResponse(reel, me.getId()));

        try {
            // record search history asynchronously; here simple call (swallow errors inside service)
            reelSearchHistoryService.recordSearch(query.trim(), (int) page.getTotalElements());
        } catch (Exception ignored) {
        }

        return page;
    }

    @Override
    public Page<ReelResponse> getLikedReels(Pageable pageable) {
        var me = currentUserProvider.get();
        return reelLikeRepository.findAllByUserIdOrderByCreatedAtDesc(me.getId(), pageable)
                .map(ReelLike::getReel)
                .map(reel -> toReelResponse(reel, me.getId()));
    }

    @Override
    public Page<ReelResponse> getMyCreatedReels(Pageable pageable) {
        var me = currentUserProvider.get();
        return reelRepository.findAllByUserIdOrderByCreatedAtDesc(me.getId(), pageable)
                .map(reel -> toReelResponse(reel, me.getId()));
    }

    @Override
    public Page<ReelResponse> getSavedReels(Pageable pageable) {
        var me = currentUserProvider.get();
        return reelSaveRepository.findAllByUserIdOrderByCreatedAtDesc(me.getId(), pageable)
                .map(ReelSave::getReel)
                .map(reel -> toReelResponse(reel, me.getId()));
    }

    @Override
    public Page<ReelResponse> getViewedReels(Pageable pageable) {
        var me = currentUserProvider.get();
        return reelViewRepository.findAllByUserIdOrderByCreatedAtDesc(me.getId(), pageable)
                .map(ReelView::getReel)
                .map(reel -> toReelResponse(reel, me.getId()));
    }

    @Override
    public ReelResponse incrementView(Long reelId) {
        var me = currentUserProvider.get();

        var reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Reel"));

        reel.setViewCount(reel.getViewCount() + 1);
        var saved = reelRepository.save(reel);

        // record view entry if not exists
        if (!reelViewRepository.existsByReelIdAndUserId(reelId, me.getId())) {
            reelViewRepository.save(new ReelView(reel, me));
        }

        return toReelResponse(saved, me.getId());
    }

    @Override
    public ReelResponse toggleLike(Long reelId) {
        var me = currentUserProvider.get();

        var reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Reel"));

        boolean liked = reelLikeRepository.existsByReelIdAndUserId(reelId, me.getId());

        if (liked) {
            reelLikeRepository.deleteByReelIdAndUserId(reelId, me.getId());
        } else {
            reelLikeRepository.save(new ReelLike(reel, me));
        }

        return toReelResponse(reel, me.getId());
    }

    @Override
    public ReelResponse toggleSave(Long reelId) {
        var me = currentUserProvider.get();

        var reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Reel"));

        boolean saved = reelSaveRepository.existsByReelIdAndUserId(reelId, me.getId());

        if (saved) {
            // remove save efficiently
            reelSaveRepository.deleteByReelIdAndUserId(reelId, me.getId());
        } else {
            reelSaveRepository.save(new me.huynhducphu.ping_me.model.reels.ReelSave(reel, me));
        }

        return toReelResponse(reel, me.getId());
    }

    @Override
    public void deleteReel(Long id) {
        var user = currentUserProvider.get();

        var reel = reelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Reel"));

        if (!reel.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xóa Reel này");
        }

        reactionRepository.deleteAllByCommentReelId(id);
        reelCommentRepository.deleteAllByReelId(id);
        reelLikeRepository.deleteAllByReelId(id);

        if (reel.getVideoUrl() != null) {
            s3Service.deleteFileByUrl(reel.getVideoUrl());
        }

        // 5) xóa reel
        reelRepository.delete(reel);
    }


    private ReelResponse toReelResponse(Reel reel, Long meId) {
        ReelResponse res = modelMapper.map(reel, ReelResponse.class);

        long likeCount = reelLikeRepository.countByReelId(reel.getId());
        long commentCount = reelCommentRepository.countByReelId(reel.getId());
        boolean isLikedByMe = reelLikeRepository.existsByReelIdAndUserId(reel.getId(), meId);
        boolean isSavedByMe = reelSaveRepository.existsByReelIdAndUserId(reel.getId(), meId);

        res.setLikeCount(likeCount);
        res.setCommentCount(commentCount);
        res.setIsLikedByMe(isLikedByMe);
        res.setIsSavedByMe(isSavedByMe);

        res.setUserId(reel.getUser().getId());
        res.setUserName(reel.getUser().getName());
        res.setUserAvatarUrl(reel.getUser().getAvatarUrl());
        res.setHashtags(reel.getHashtags());
        return res;
    }

    @Override
    public ReelResponse updateReel(Long reelId, ReelRequest dto, MultipartFile video) {
        var user = currentUserProvider.get();

        var reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Reel"));

        if (!reel.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền cập nhật Reel này");
        }

        if (dto.getCaption() != null) {
            reel.setCaption(dto.getCaption());
        }

        if (dto.getHashtags() != null) {
            List<String> normalized = dto.getHashtags().stream()
                    .filter(h -> h != null && !h.isBlank())
                    .map(String::trim)
                    .map(h -> h.startsWith("#") ? h.substring(1) : h)
                    .map(String::toLowerCase)
                    .distinct()
                    .collect(Collectors.toList());
            reel.setHashtags(normalized);
        }

        if (video != null && !video.isEmpty()) {
            String contentType = video.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                throw new IllegalArgumentException("File upload phải là video");
            }

            // xóa video cũ trước
            if (reel.getVideoUrl() != null) {
                s3Service.deleteFileByUrl(reel.getVideoUrl());
            }

            String original = video.getOriginalFilename();
            String ext = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf("."))
                    : ".mp4";

            String randomFileName = UUID.randomUUID() + ext;
            long maxBytes = maxReelVideoSize.toBytes();

            String url = s3Service.uploadFile(
                    video,
                    reelsFolder,
                    randomFileName,
                    true,
                    maxBytes
            );

            reel.setVideoUrl(url);
        }

        var saved = reelRepository.save(reel);
        return toReelResponse(saved, user.getId());
    }

}
