package me.huynhducphu.ping_me.utils.mapper;

import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.config.websocket.auth.UserSocketPrincipal;
import me.huynhducphu.ping_me.dto.response.authentication.CurrentUserSessionResponse;
import me.huynhducphu.ping_me.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Admin 1/9/2026
 *
 **/
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ModelMapper modelMapper;

    public CurrentUserSessionResponse mapToCurrentUserSessionResponse(User user) {
        var res = modelMapper.map(user, CurrentUserSessionResponse.class);

        var roleName = user.getRole() != null ? user.getRole().getName() : "";
        res.setRoleName(roleName);
        return res;
    }

    public UserSocketPrincipal extractUserPrincipal(Principal principal) {
        if (principal instanceof Authentication auth) {
            Object receivedPrincipal = auth.getPrincipal();
            if (receivedPrincipal instanceof UserSocketPrincipal) {
                return (UserSocketPrincipal) receivedPrincipal;
            }
        }

        if (principal instanceof UserSocketPrincipal) {
            return (UserSocketPrincipal) principal;
        }
        return null;
    }

}
