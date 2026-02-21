package org.ping_me.service.reel;

import org.ping_me.dto.request.reels.UpsertReelRequest;
import org.ping_me.dto.response.reels.ReelResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ReelService {

    /**
     * =====================================
     * TẠO - CẬP NHẬT - XÓA REEL
     * =====================================
     */

    ReelResponse createReel(UpsertReelRequest upsertReelRequest, MultipartFile video);

    ReelResponse updateReel(Long reelId, UpsertReelRequest upsertReelRequest);

    void deleteReel(Long id);

    /**
     * =====================================
     * LẤY REEL
     * =====================================
     */

    Page<ReelResponse> getFeed(Pageable pageable);

    ReelResponse incrementView(Long reelId);

    ReelResponse toggleLike(Long reelId);

    ReelResponse toggleSave(Long reelId);


    // List reels liked by the current user
    Page<ReelResponse> getLikedReels(Pageable pageable);

    // List reels saved/favorited by the current user
    Page<ReelResponse> getSavedReels(Pageable pageable);

    // List reels viewed by the current user
    Page<ReelResponse> getViewedReels(Pageable pageable);

    // Search reels by title/caption
    Page<ReelResponse> searchByTitle(String query, Pageable pageable);

    // List reels created by the current user
    Page<ReelResponse> getMyCreatedReels(Pageable pageable);
}
