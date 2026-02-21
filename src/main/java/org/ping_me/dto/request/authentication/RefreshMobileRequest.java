package org.ping_me.dto.request.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin 1/10/2026
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RefreshMobileRequest {

    private String refreshToken;
    private SubmitSessionMetaRequest submitSessionMetaRequest;

}
