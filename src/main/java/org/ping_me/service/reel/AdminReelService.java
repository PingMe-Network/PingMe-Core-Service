package org.ping_me.service.reel;

import org.ping_me.dto.request.reels.AdminReelFilterRequest;
import org.ping_me.dto.response.reels.AdminReelResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminReelService {
    Page<AdminReelResponse> getReels(AdminReelFilterRequest filter, Pageable pageable);

    AdminReelResponse getDetail(Long reelId);

    AdminReelResponse updateCaption(Long reelId, String caption);

    AdminReelResponse hideReel(Long reelId, String adminNote);

    void hardDelete(Long reelId);
}
