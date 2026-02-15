package me.huynhducphu.ping_me.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.admin.request.user.CreateUserRequest;
import me.huynhducphu.ping_me.dto.admin.request.user.UpdateAccountStatusRequest;
import me.huynhducphu.ping_me.dto.admin.response.user.DefaultUserResponse;
import me.huynhducphu.ping_me.dto.base.ApiResponse;
import me.huynhducphu.ping_me.dto.base.PageResponse;
import me.huynhducphu.ping_me.model.constant.AccountStatus;
import me.huynhducphu.ping_me.service.user.UserManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin 8/3/2025
 **/
@Tag(
        name = "Admin Users",
        description = "Admin quản lý người dùng hệ thống"
)
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Operation(
            summary = "Tạo người dùng mới",
            description = "Admin tạo mới một người dùng trong hệ thống"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<DefaultUserResponse>> saveUser(
            @Parameter(description = "Thông tin tạo người dùng", required = true)
            @RequestBody @Valid CreateUserRequest createUserRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(userManagementService.saveUser(createUserRequest)));
    }

    @Operation(
            summary = "Lấy danh sách người dùng",
            description = "Admin lấy danh sách người dùng có phân trang"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DefaultUserResponse>>> getAllUsers(
            @Parameter(description = "Thông tin phân trang")
            @PageableDefault(size = 5) Pageable pageable,
            @RequestParam(required = false) AccountStatus accountStatus
    ) {
        Page<DefaultUserResponse> defaultUserResponseDtoPage =
                userManagementService.getAllUsers(pageable, accountStatus);

        PageResponse<DefaultUserResponse> pageResponse =
                new PageResponse<>(defaultUserResponseDtoPage);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(pageResponse));
    }

    @Operation(
            summary = "Lấy chi tiết người dùng",
            description = "Admin lấy thông tin chi tiết người dùng theo ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DefaultUserResponse>> getUserById(
            @Parameter(description = "ID người dùng", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(userManagementService.getUserById(id)));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateAccountStatusById(
            @PathVariable Long id,
            @RequestBody UpdateAccountStatusRequest request
    ) {
        boolean isDeleted = userManagementService.updateAccountStatusById(id, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse<>(isDeleted));
    }
}
