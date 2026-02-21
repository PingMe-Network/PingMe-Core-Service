package org.ping_me.dto.request.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin 8/16/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubmitSessionMetaRequest {

    private String deviceType;
    private String browser;
    private String os;

}
