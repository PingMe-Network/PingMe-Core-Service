package me.huynhducphu.ping_me.service.user.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.huynhducphu.ping_me.dto.request.user.CreateUserRequest;
import me.huynhducphu.ping_me.dto.request.user.UpdateAccountStatusRequest;
import me.huynhducphu.ping_me.dto.response.user.DefaultUserResponse;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.constant.AccountStatus;
import me.huynhducphu.ping_me.model.constant.AuthProvider;
import me.huynhducphu.ping_me.repository.jpa.auth.UserRepository;
import me.huynhducphu.ping_me.service.user.UserManagementService;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import org.modelmapper.ModelMapper;
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
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    ModelMapper modelMapper;
    CurrentUserProvider currentUserProvider;

    @Override
    public DefaultUserResponse saveUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.getEmail()))
            throw new DataIntegrityViolationException("Email đã tồn tại");

        var user = modelMapper.map(createUserRequest, User.class);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        var savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, DefaultUserResponse.class);
    }

    @Override
    public Page<DefaultUserResponse> getAllUsers(Pageable pageable, AccountStatus accountStatus) {
        if (accountStatus == null)
            return userRepository.findAll(pageable)
                    .map(user -> modelMapper.map(user, DefaultUserResponse.class));
        return userRepository.findByAccountStatus(accountStatus, pageable)
                .map(user -> modelMapper.map(user, DefaultUserResponse.class));
    }

    @Override
    public DefaultUserResponse getUserById(Long id) {
        return userRepository
                .findById(id)
                .map(user -> modelMapper.map(user, DefaultUserResponse.class))
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
