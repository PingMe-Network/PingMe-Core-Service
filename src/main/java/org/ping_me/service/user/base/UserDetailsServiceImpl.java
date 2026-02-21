package org.ping_me.service.user.base;

import lombok.RequiredArgsConstructor;
import org.ping_me.repository.jpa.auth.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Admin 8/4/2025
 **/
// Lớp này override cách Spring Security truy vấn thông tin người dùng.
// Thay vì sử dụng InMemoryUserDetailsManager
//
// Lớp này định nghĩa logic UserDetailsService.
@Service("userDetailsService")
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Truy vấn User trong DB theo email.
        // Nếu không tìm thấy sẽ ném ra UsernameNotFoundException.
        // (User ở đây là entity: me.huynhducphu.PingMe_Backend.model.User)
        var user = userRepository
                .getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        // Chuyển đổi entity User sang User của Spring Security,
        // dùng để xác thực và lưu trong SecurityContext.
        // (User ở đây là org.springframework.security.core.userdetails.User)
        return User
                .builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.emptyList())
                .build();
    }
}

