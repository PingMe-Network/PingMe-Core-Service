package org.ping_me.model.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Admin 8/10/2025
 **/
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoomMemberId implements Serializable {
    private Long roomId;
    private Long userId;
}
