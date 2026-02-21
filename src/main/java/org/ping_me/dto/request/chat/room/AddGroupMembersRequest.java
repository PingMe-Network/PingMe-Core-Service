package org.ping_me.dto.request.chat.room;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Admin 11/19/2025
 *
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddGroupMembersRequest {

    @NotNull(message = "Mã phòng không được để trống")
    private Long roomId;

    @NotEmpty(message = "Danh sách thành viên không được để trống")
    private List<Long> memberIds;

}
