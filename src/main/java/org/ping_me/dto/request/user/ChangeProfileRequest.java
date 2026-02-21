package org.ping_me.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ping_me.model.constant.Gender;

import java.time.LocalDate;

/**
 * Admin 8/13/2025
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChangeProfileRequest {

    @NotBlank(message = "Tên người dùng được để trống")
    private String name;

    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    private String address;

    private LocalDate dob;

}
