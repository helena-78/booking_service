package com.sportlink.notificationservice.service;

import com.sportlink.notificationservice.dto.request.DispatchNotificationRequest;
import com.sportlink.notificationservice.dto.response.NotificationResponse;
import com.sportlink.notificationservice.exception.ResourceNotFoundException;
import com.sportlink.notificationservice.model.Notification;
import com.sportlink.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse dispatch(DispatchNotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .type(request.getType())
                .channel(request.getChannel())
                .payload(request.getPayload())
                .read(false)
                .build();
        Notification saved = notificationRepository.save(notification);
        log.info("Dispatched notification [{}] via [{}] to user [{}]",
                request.getType(), request.getChannel(), request.getRecipientId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getInbox(UUID userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID userId, UUID notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (!n.getRecipientId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found: " + notificationId);
        }
        n.setRead(true);
        return toResponse(notificationRepository.save(n));
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientId(n.getRecipientId())
                .type(n.getType())
                .channel(n.getChannel())
                .payload(n.getPayload())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
