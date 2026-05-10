package com.sportlink.notificationservice.repository;

import com.sportlink.notificationservice.model.Channel;
import com.sportlink.notificationservice.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    List<Notification> findByRecipientIdAndReadFalseAndChannel(UUID recipientId, Channel channel);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipientId = :recipientId AND n.read = false AND n.channel = 'IN_APP'")
    void markAllAsReadForUser(UUID recipientId);
}
