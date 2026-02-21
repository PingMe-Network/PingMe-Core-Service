package org.ping_me.dto.response.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin 8/3/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
// ================================================================
// Class này đại diện cho thông tin phiên làm việc của người dùng hiện tại
// ================================================================
public class CurrentUserSessionResponse {

    private Long id;
    private String email;
    private String name;
    private String avatarUrl;
    private String updatedAt;
    private String roleName;

}
