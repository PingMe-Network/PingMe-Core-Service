package org.ping_me.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin 8/16/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeviceMeta {

    private String sessionId;
    private String deviceType;
    private String browser;
    private String os;
    private String lastActiveAt;

}
