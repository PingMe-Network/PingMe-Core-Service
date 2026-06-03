package org.ping_me.service.chat.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ping_me.model.chat.Message;
import org.ping_me.model.chat.ChatReminder;
import org.ping_me.model.constant.MessageType;
import org.ping_me.model.constant.ReminderStatus;
import org.ping_me.repository.jpa.chat.ChatReminderRepository;
import org.ping_me.repository.jpa.chat.RoomParticipantRepository;
import org.ping_me.repository.mongodb.chat.MessageRepository;
import org.ping_me.service.chat.MessageCachingService;
import org.ping_me.service.chat.event.message.MessageCreatedEvent;
import org.ping_me.service.chat.event.message.MessageUpdatedEvent;
import org.ping_me.service.chat.event.room.RoomUpdatedEvent;
import org.ping_me.utils.mapper.ChatMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.DateTimeException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatReminderScheduler {

    private static final int BATCH_SIZE = 100;

    private final ChatReminderRepository chatReminderRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final MessageRepository messageRepository;
    private final MessageCachingService messageCachingService;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatMapper chatMapper;

    @Value("${app.messages.cache.enabled}")
    private boolean cacheEnabled;

    @Scheduled(fixedDelayString = "${app.chat.reminders.scan-delay-ms:10000}")
    @Transactional
    public void triggerDueReminders() {
        var now = Instant.now();
        var pendingReminders = chatReminderRepository.findByStatus(
                ReminderStatus.PENDING,
                PageRequest.of(0, BATCH_SIZE, Sort.by("remindAt").ascending())
        );

        for (ChatReminder reminder : pendingReminders) {
            if (isDue(reminder, now)) {
                triggerReminder(reminder, LocalDateTime.now());
            }
        }
    }

    private boolean isDue(ChatReminder reminder, Instant now) {
        try {
            var zoneId = ZoneId.of(reminder.getTimezone());
            var reminderInstant = reminder.getRemindAt().atZone(zoneId).toInstant();
            return !reminderInstant.isAfter(now);
        } catch (DateTimeException ex) {
            log.warn("Reminder {} has invalid timezone {}", reminder.getId(), reminder.getTimezone());
            return !reminder.getRemindAt().isAfter(LocalDateTime.now());
        }
    }

    private void triggerReminder(ChatReminder reminder, LocalDateTime triggeredAt) {
        reminder.setStatus(ReminderStatus.TRIGGERED);
        reminder.setTriggeredAt(triggeredAt);

        messageRepository.findById(reminder.getMessageId())
                .ifPresentOrElse(
                        message -> {
                            syncReminderMessage(message);
                            createReminderAlertMessage(reminder, triggeredAt);
                        },
                        () -> log.warn("Reminder {} has missing message {}", reminder.getId(), reminder.getMessageId())
                );
    }

    private void syncReminderMessage(Message message) {
        if (cacheEnabled) {
            messageCachingService.updateMessage(
                    message.getRoomId(),
                    message.getId(),
                    chatMapper.toMessageResponseDto(message)
            );
        }

        eventPublisher.publishEvent(new MessageUpdatedEvent(message));
    }

    private void createReminderAlertMessage(ChatReminder reminder, LocalDateTime triggeredAt) {
        var room = reminder.getRoom();
        var alertMessage = new Message();
        alertMessage.setRoomId(room.getId());
        alertMessage.setSenderId(reminder.getCreatedByUser().getId());
        alertMessage.setType(MessageType.SYSTEM);
        alertMessage.setContent("Nhắc hẹn: " + reminder.getTitle());
        alertMessage.setIsActive(true);
        alertMessage.setCreatedAt(triggeredAt);
        alertMessage.setClientMsgId(UUID.randomUUID());

        alertMessage = messageRepository.save(alertMessage);

        room.setLastMessageId(alertMessage.getId());
        room.setLastMessageAt(alertMessage.getCreatedAt());

        if (cacheEnabled) {
            messageCachingService.cacheNewMessage(
                    room.getId(),
                    chatMapper.toMessageResponseDto(alertMessage)
            );
        }

        eventPublisher.publishEvent(new MessageCreatedEvent(alertMessage));
        eventPublisher.publishEvent(new RoomUpdatedEvent(
                room,
                roomParticipantRepository.findByRoom_Id(room.getId()),
                alertMessage
        ));
    }
}
