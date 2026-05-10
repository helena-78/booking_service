package com.sportlink.notificationservice.dto.request;

import com.sportlink.notificationservice.model.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Device push token registration request")
public class DeviceTokenRequest {

    @NotBlank
    @Schema(description = "FCM/APNs device token", example = "fcm-token-abc123")
    private String token;

    @NotNull
    @Schema(description = "Device platform", example = "ANDROID")
    private Platform platform;
}
