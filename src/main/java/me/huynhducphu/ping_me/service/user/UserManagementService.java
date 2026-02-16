package me.huynhducphu.ping_me.service.user;

import me.huynhducphu.ping_me.dto.request.user.CreateUserRequest;
import me.huynhducphu.ping_me.dto.request.user.UpdateAccountStatusRequest;
import me.huynhducphu.ping_me.dto.response.user.DefaultUserResponse;
import me.huynhducphu.ping_me.model.constant.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin 8/3/2025
 **/
public interface UserManagementService {
    DefaultUserResponse saveUser(CreateUserRequest createUserRequest);

    Page<DefaultUserResponse> getAllUsers(Pageable pageable, AccountStatus accountStatus);

    DefaultUserResponse getUserById(Long id);

    boolean updateAccountStatusById(Long id, UpdateAccountStatusRequest request);
}
