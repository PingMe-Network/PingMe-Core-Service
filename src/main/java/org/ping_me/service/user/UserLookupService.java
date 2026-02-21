package org.ping_me.service.user;

import org.ping_me.dto.response.user.UserSummaryResponse;
import org.ping_me.dto.response.user.UserSummarySimpleResponse;

/**
 * Admin 8/21/2025
 **/
public interface UserLookupService {
    UserSummaryResponse lookupUser(String email);

    UserSummarySimpleResponse lookupUserById(Long userId);
}
