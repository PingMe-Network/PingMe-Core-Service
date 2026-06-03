package org.ping_me.repository.jpa.chat;

import org.ping_me.model.chat.ChatReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatReminderRepository extends JpaRepository<ChatReminder, Long> {
    Optional<ChatReminder> findByMessageId(String messageId);
}
