package me.huynhducphu.ping_me.repository.mongodb.ai;

import me.huynhducphu.ping_me.model.ai.AIMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Le Tran Gia Huy
 * @created 28/01/2026 - 3:19 PM
 * @project PingMe-Backend
 * @package me.huynhducphu.ping_me.repository.mongodb.ai
 */

@Repository
public interface AIMessageRepository extends MongoRepository<AIMessage, UUID> {
    @NotNull
    @Override
    List<AIMessage> findAll();

    @NotNull
    @Override
    Optional<AIMessage> findById(@NotNull UUID uuid);

    Page<AIMessage> findByChatRoomIdAndUserIdEqualsIgnoreCaseOrderByCreatedAtDesc(
            UUID chatRoomId,
            Long userId,
            Pageable pageable
    );

    Slice<AIMessage> findByChatRoomIdOrderByCreatedAtDesc(
            UUID chatRoomId,
            Pageable pageable
    );

    Page<AIMessage> findByUserIdAndChatRoomIdNotOrderByCreatedAtDesc(
            Long userId,
            UUID chatRoomId,
            Pageable pageable
    );

    @NotNull
    @Override
    <S extends AIMessage> S save(@NotNull S entity);


}
