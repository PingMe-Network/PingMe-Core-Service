package me.huynhducphu.ping_me.controller.authentication;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.request.authentication.LoginRequest;
import me.huynhducphu.ping_me.dto.response.authentication.AdminLoginResponse;
import me.huynhducphu.ping_me.service.authentication.AuthenticationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 5/02/2026, Thursday
 **/

@RestController
@RequestMapping("/auth/admin")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AdminWebAuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AdminLoginResponse> login(@Valid @RequestBody LoginRequest request){
        return new ApiResponse<>(authenticationService.adminLogin(request));
    }
}
