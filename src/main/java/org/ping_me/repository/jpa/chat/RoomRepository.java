package org.ping_me.repository.jpa.chat;

import org.ping_me.model.chat.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Admin 8/25/2025
 *
 **/
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByDirectKey(String directKey);

    @Query("""
                select r from Room r
                join RoomParticipant rp on rp.room = r
                where rp.id.userId = :userId
                order by r.lastMessageAt desc nulls last, r.id desc
            """)
    Page<Room> findAllByMember(@Param("userId") Long userId, Pageable pageable);
}
