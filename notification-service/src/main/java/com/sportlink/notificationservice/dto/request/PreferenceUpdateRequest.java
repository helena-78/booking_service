package com.sportlink.notificationservice.dto.request;

import com.sportlink.notificationservice.model.Channel;
import com.sportlink.notificationservice.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Enable or disable a specific notification type on a specific channel")
public class PreferenceUpdateRequest {

    @NotNull
    @Schema(description = "Event type to configure", example = "FOLLOWER_UPDATE")
    private NotificationType eventType;

    @NotNull
    @Schema(description = "Channel to configure", example = "IN_APP")
    private Channel channel;

    @NotNull
    @Schema(description = "true = enabled, false = disabled", example = "true")
    private Boolean enabled;
}
