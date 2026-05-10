package com.sportlink.notificationservice.dto.response;

import com.sportlink.notificationservice.model.Channel;
import com.sportlink.notificationservice.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "A notification in the user's inbox")
public class NotificationResponse {
    private UUID id;
    private UUID recipientId;
    private NotificationType type;
    private Channel channel;
    private String payload;
    private boolean read;
    private LocalDateTime createdAt;
}
