package me.huynhducphu.ping_me.repository.mongodb.ai;

import me.huynhducphu.ping_me.model.ai.AIChatRoom;
import org.jetbrains.annotations.NotNull;
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
public interface AIChatRoomRepository extends MongoRepository<AIChatRoom, UUID> {
    @NotNull
    @Override
    List<AIChatRoom> findAll();

    @NotNull
    @Override
    Optional<AIChatRoom> findById(@NotNull UUID uuid);

    // Trong AIChatRoomRepository
    Slice<AIChatRoom> findByUserIdOrderByUpdatedAtDesc(
            Long userId,
            Pageable pageable
    );

    List<AIChatRoom> findTop5ByUserIdAndIdNotOrderByUpdatedAtDesc(
            Long userId,
            UUID id
    );

    @NotNull
    @Override
    <S extends AIChatRoom> S save(@NotNull S entity);
}
