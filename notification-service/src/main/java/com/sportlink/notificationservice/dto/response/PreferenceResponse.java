package com.sportlink.notificationservice.dto.response;

import com.sportlink.notificationservice.model.Channel;
import com.sportlink.notificationservice.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "A single notification preference entry")
public class PreferenceResponse {
    private UUID id;
    private NotificationType eventType;
    private Channel channel;
    private boolean enabled;
}
