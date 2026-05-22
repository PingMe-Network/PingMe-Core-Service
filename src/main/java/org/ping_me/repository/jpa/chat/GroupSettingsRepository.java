package org.ping_me.repository.jpa.chat;

import org.ping_me.model.chat.GroupSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupSettingsRepository extends JpaRepository<GroupSettings, Long> {
    Optional<GroupSettings> findByRoomId(Long roomId);

    Optional<GroupSettings> findByJoinLinkToken(String joinLinkToken);
}
