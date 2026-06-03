package org.ping_me.repository.jpa.chat;

import org.ping_me.model.chat.ChatNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatNoteRepository extends JpaRepository<ChatNote, Long> {
    Optional<ChatNote> findByMessageId(String messageId);
}
