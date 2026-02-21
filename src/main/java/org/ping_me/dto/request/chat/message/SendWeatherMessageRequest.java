package org.ping_me.dto.request.chat.message;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin 11/23/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SendWeatherMessageRequest {

    @NotNull(message = "Mã phòng không được để trống")
    private Long roomId;

    @NotNull(message = "Vĩ độ không được để trống")
    @DecimalMin(value = "-90.0", inclusive = true, message = "Vĩ độ phải >= -90")
    @DecimalMax(value = "90.0", inclusive = true, message = "Vĩ độ phải <= 90")
    private double lat;

    @NotNull(message = "Kinh độ không được để trống")
    @DecimalMin(value = "-180.0", inclusive = true, message = "Kinh độ phải >= -180")
    @DecimalMax(value = "180.0", inclusive = true, message = "Kinh độ phải <= 180")
    private double lon;

    @NotBlank(message = "UUID không được để trống")
    private String clientMsgId;

}
