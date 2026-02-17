package me.huynhducphu.ping_me.dto.response.user;

import lombok.*;

/**
 * @author Le Tran Gia Huy
 * @created 09/12/2025 - 1:27 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.dto.response.common
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserSummarySimpleResponse {
    private Long id;
    private String name;
    private String avatarUrl;
}
