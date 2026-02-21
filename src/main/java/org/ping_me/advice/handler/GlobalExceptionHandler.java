package org.ping_me.advice.handler;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.ping_me.advice.base.ErrorCode;
import org.ping_me.dto.base.ApiResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * Admin 7/31/2025
 **/
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =========================================================================
    // GROUP 1: SYSTEM & COMMON (10xx)
    // =========================================================================

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Critical System Error: ", e);

        ErrorCode error = ErrorCode.UNCATEGORIZED_EXCEPTION;
        return ResponseEntity
                .status(error.getStatusCode())
                .body(new ApiResponse<>(error.getMessage(), error.getCode()));
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(Exception e) {
        log.warn("Method not supported: {}", e.getMessage());

        ErrorCode error = ErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity
                .status(error.getStatusCode())
                .body(new ApiResponse<>(error.getMessage(), error.getCode()));
    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleParameterExceptions(Exception e) {
        ErrorCode error = ErrorCode.INVALID_PARAMETER;
        String msg = error.getMessage();

        if (e instanceof MethodArgumentTypeMismatchException ex) {
            String typeName = ex.getRequiredType() != null
                    ? ex.getRequiredType().getSimpleName() : "valid type";

            msg = ex.getName() + " phải là " + typeName;
        }
        return ResponseEntity
                .status(error.getStatusCode())
                .body(new ApiResponse<>(msg, error.getCode()));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(e.getMessage(), ErrorCode.INVALID_ARGUMENT.getCode()));
    }

    // =========================================================================
    // GROUP 2: SECURITY & AUTHENTICATION (11xx)
    // =========================================================================

    @ExceptionHandler(value = {
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(Exception e) {
        ErrorCode error = ErrorCode.INVALID_CREDENTIALS;

        return ResponseEntity
                .status(error.getStatusCode())
                .body(new ApiResponse<>(error.getMessage(), error.getCode()));
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorization(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(e.getMessage(), ErrorCode.UNAUTHORIZED.getCode()));
    }

    @ExceptionHandler(value = DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handlingDisabledException(DisabledException exception){
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setErrorCode(errorCode.getCode());
        apiResponse.setErrorMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }
    // =========================================================================
    // GROUP 3: VALIDATION (12xx)
    // =========================================================================

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(msg, ErrorCode.INVALID_KEY.getCode()));
    }

    // =========================================================================
    // GROUP 4: DATA & PERSISTENCE (30xx)
    // =========================================================================

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(e.getMessage(), ErrorCode.ENTITY_NOT_FOUND.getCode()));
    }

    @ExceptionHandler(value = {
            SQLIntegrityConstraintViolationException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleDataConflict(Exception e) {
        log.error("DB Error: {}", e.getMessage());

        ErrorCode error = ErrorCode.DATA_INTEGRITY_VIOLATION;
        return ResponseEntity
                .status(error.getStatusCode())
                .body(new ApiResponse<>(error.getMessage(), error.getCode()));
    }
}
