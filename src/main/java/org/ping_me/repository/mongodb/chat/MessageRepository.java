package org.ping_me.repository.mongodb.chat;

import org.ping_me.model.chat.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Admin 8/25/2025
 *
 **/
@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    Optional<Message> findByRoomIdAndSenderIdAndClientMsgId(
            Long roomId,
            Long senderId,
            UUID clientMsgId
    );

    List<Message> findByRoomIdAndIdLessThanOrderByIdDesc(
            Long roomId,
            String idIsLessThan,
            Pageable pageable
    );

    List<Message> findByRoomIdOrderByIdDesc(
            Long roomId,
            Pageable pageable
    );

    long countByRoomIdAndCreatedAtGreaterThan(
            Long roomId,
            LocalDateTime createdAt
    );


}
