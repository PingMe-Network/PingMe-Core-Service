package org.ping_me.service.user.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.ping_me.dto.request.user.CreateUserRequest;
import org.ping_me.dto.request.user.UpdateAccountStatusRequest;
import org.ping_me.dto.response.user.DefaultUserResponse;
import org.ping_me.model.User;
import org.ping_me.model.constant.AccountStatus;
import org.ping_me.model.constant.AuthProvider;
import org.ping_me.repository.jpa.auth.UserRepository;
import org.ping_me.service.user.CurrentUserProvider;
import org.ping_me.service.user.UserManagementService;
import org.ping_me.utils.mapper.UserMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Admin 8/3/2025
 **/
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    // Repository
    UserRepository userRepository;

    // Encoder
    PasswordEncoder passwordEncoder;

    // Provider
    CurrentUserProvider currentUserProvider;

    // Mapper
    UserMapper userMapper;

    @Override
    public DefaultUserResponse saveUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.getEmail()))
            throw new DataIntegrityViolationException("Email đã tồn tại");

        var user = User
                .builder()
                .email(createUserRequest.getEmail())
                .name(createUserRequest.getName())
                .build();

        user.setAuthProvider(AuthProvider.LOCAL);
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        var savedUser = userRepository.save(user);

        return userMapper.mapToDefaultUserResponse(savedUser);
    }

    @Override
    public Page<@NonNull DefaultUserResponse> getAllUsers(Pageable pageable, AccountStatus accountStatus) {
        if (accountStatus == null)
            return userRepository.findAll(pageable)
                    .map(userMapper::mapToDefaultUserResponse);
        return userRepository.findByAccountStatus(accountStatus, pageable)
                .map(userMapper::mapToDefaultUserResponse);
    }

    @Override
    public DefaultUserResponse getUserById(Long id) {
        return userRepository
                .findById(id)
                .map(userMapper::mapToDefaultUserResponse)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với id này"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public boolean updateAccountStatusById(Long id, UpdateAccountStatusRequest request) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new NullPointerException("Không tìm thấy tài khoản!"));
            if (Objects.equals(user.getId(), currentUserProvider.get().getId()))
                throw new IllegalArgumentException("Không thể thay đổi trạng thái tài khoản của chính mình!");

            user.setAccountStatus(request.getAccountStatus());

            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
