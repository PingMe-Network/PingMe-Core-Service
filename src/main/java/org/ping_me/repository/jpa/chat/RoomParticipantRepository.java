package org.ping_me.repository.jpa.chat;

import org.ping_me.model.chat.RoomParticipant;
import org.ping_me.model.common.RoomMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Admin 8/25/2025
 *
 **/
@Repository
public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, RoomMemberId> {
    List<RoomParticipant> findByRoom_Id(Long roomId);

    List<RoomParticipant> findByRoom_IdIn(List<Long> roomIds);

    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.room.id = :roomId AND rp.user.id != :senderId")
    Optional<RoomParticipant> findOtherParticipantInRoom(@Param("roomId") Long roomId, @Param("senderId") Long senderId);

    @Query("SELECT rp.user.id FROM RoomParticipant rp WHERE rp.room.id = :roomId AND rp.user.id != :senderId")
    List<Long> findAllUserIdsInRoomExcept(@Param("roomId") Long roomId, @Param("senderId") Long senderId);
}
