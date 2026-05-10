package com.sportlink.notificationservice.event.consumer;

import com.sportlink.notificationservice.config.RabbitMQConfig;
import com.sportlink.notificationservice.model.Channel;
import com.sportlink.notificationservice.model.Notification;
import com.sportlink.notificationservice.model.NotificationType;
import com.sportlink.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InteractionEventConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitMQConfig.USER_FOLLOWED_QUEUE)
    public void handleUserFollowed(Map<String, Object> event) {
        UUID followeeId = UUID.fromString(event.get("followeeId").toString());
        UUID followerId = UUID.fromString(event.get("followerId").toString());
        log.info("UserFollowed: {} → {}", followerId, followeeId);

        notificationRepository.save(Notification.builder()
                .recipientId(followeeId)
                .type(NotificationType.FOLLOWER_UPDATE)
                .channel(Channel.IN_APP)
                .payload(String.format(
                        "{\"message\":\"User %s started following you\",\"followerId\":\"%s\"}",
                        followerId, followerId))
                .read(false)
                .build());
    }

    @RabbitListener(queues = RabbitMQConfig.COMMENT_POSTED_QUEUE)
    public void handleCommentPosted(Map<String, Object> event) {
        UUID activityId = UUID.fromString(event.get("activityId").toString());
        UUID authorId   = UUID.fromString(event.get("authorId").toString());
        log.info("CommentPosted on activity {} by {}", activityId, authorId);

        Object ownerRaw = event.get("activityOwnerId");
        if (ownerRaw == null) {
            log.debug("No activityOwnerId in CommentPosted event — skipping notification");
            return;
        }

        UUID ownerId = UUID.fromString(ownerRaw.toString());
        if (ownerId.equals(authorId)) {
            // Activity owner commented on their own activity — no self-notification
            return;
        }

        notificationRepository.save(Notification.builder()
                .recipientId(ownerId)
                .type(NotificationType.COMMENT_POSTED)
                .channel(Channel.IN_APP)
                .payload(String.format(
                        "{\"message\":\"New comment on your activity\",\"activityId\":\"%s\",\"authorId\":\"%s\"}",
                        activityId, authorId))
                .read(false)
                .build());
    }
}
