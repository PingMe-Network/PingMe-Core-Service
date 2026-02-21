package org.ping_me.advice.base;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Admin 11/25/2025
 *
 **/
@Getter
public enum ErrorCode {
    
    // ===== System & Common (1000 - 1099) =====
    UNCATEGORIZED_EXCEPTION(1099, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    METHOD_NOT_ALLOWED(1001, "API không hỗ trợ phương thức này", HttpStatus.METHOD_NOT_ALLOWED),
    INVALID_PARAMETER(1003, "Tham số không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_ARGUMENT(1005, "Đối số truyền vào không hợp lệ", HttpStatus.BAD_REQUEST),

    // ===== Security & Authentication (1100 - 1199) =====
    UNAUTHORIZED(1199, "Bạn không có quyền truy cập tài nguyên này!", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1102, "Token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(1103, "Tài khoản hoặc mật khẩu không hợp lệ", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1101, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),

    // ===== Validation (1200 - 1299) =====
    INVALID_KEY(1200, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),

    // ===== Data & Persistence (3000 - 3099) =====
    ENTITY_NOT_FOUND(3000, "Không tìm thấy dữ liệu yêu cầu", HttpStatus.NOT_FOUND),
    DATA_INTEGRITY_VIOLATION(3001, "Dữ liệu vi phạm ràng buộc hệ thống", HttpStatus.CONFLICT),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
