package com.sportlink.notificationservice.dto.request;

import com.sportlink.notificationservice.model.Channel;
import com.sportlink.notificationservice.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Internal request (service-to-service) to dispatch a notification")
public class DispatchNotificationRequest {

    @NotNull
    @Schema(description = "Recipient user ID")
    private UUID recipientId;

    @NotNull
    @Schema(description = "Notification type", example = "FOLLOWER_UPDATE")
    private NotificationType type;

    @NotNull
    @Schema(description = "Delivery channel", example = "IN_APP")
    private Channel channel;

    @Schema(description = "JSON payload with message body and deep-link data",
            example = "{\"message\": \"User X started following you\"}")
    private String payload;
}
