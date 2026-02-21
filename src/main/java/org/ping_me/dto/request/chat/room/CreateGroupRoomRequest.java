package org.ping_me.dto.request.chat.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Admin 11/4/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateGroupRoomRequest {

    @NotBlank(message = "Tên nhóm không được để trống")
    private String name;

    @NotEmpty(message = "Danh sách thành viên không được để trống")
    @Size(min = 2, message = "Nhóm phải có ít nhất 2 thành viên khác ngoài bạn")
    private List<Long> memberIds;

}
