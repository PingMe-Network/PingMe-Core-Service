package org.ping_me.service.chat.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ping_me.model.chat.ChatReminder;
import org.ping_me.model.constant.ReminderStatus;
import org.ping_me.repository.jpa.chat.ChatReminderRepository;
import org.ping_me.repository.mongodb.chat.MessageRepository;
import org.ping_me.service.chat.event.message.MessageUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatReminderScheduler {

    private static final int BATCH_SIZE = 100;

    private final ChatReminderRepository chatReminderRepository;
    private final MessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedDelayString = "${app.chat.reminders.scan-delay-ms:10000}")
    @Transactional
    public void triggerDueReminders() {
        var now = LocalDateTime.now();
        var dueReminders = chatReminderRepository.findByStatusAndRemindAtLessThanEqual(
                ReminderStatus.PENDING,
                now,
                PageRequest.of(0, BATCH_SIZE)
        );

        for (ChatReminder reminder : dueReminders) {
            triggerReminder(reminder, now);
        }
    }

    private void triggerReminder(ChatReminder reminder, LocalDateTime triggeredAt) {
        reminder.setStatus(ReminderStatus.TRIGGERED);
        reminder.setTriggeredAt(triggeredAt);

        messageRepository.findById(reminder.getMessageId())
                .ifPresentOrElse(
                        message -> eventPublisher.publishEvent(new MessageUpdatedEvent(message)),
                        () -> log.warn("Reminder {} has missing message {}", reminder.getId(), reminder.getMessageId())
                );
    }
}
