package org.ping_me.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin 8/3/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String errorMessage;
    private Integer errorCode;
    private T data;

    // ==================================
    // Trả về API Response chứa lỗi
    // data sẽ null
    // ==================================
    public ApiResponse(String errorMessage, Integer errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.data = null;
    }

    // ==================================
    // Trả về API Response bình thường
    // errorMessage và errorCode sẽ null
    // ==================================
    public ApiResponse(T data) {
        this.data = data;
        this.errorMessage = null;
        this.errorCode = null;
    }
}
