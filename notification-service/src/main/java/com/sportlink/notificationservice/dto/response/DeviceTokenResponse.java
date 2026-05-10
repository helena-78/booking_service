package com.sportlink.notificationservice.dto.response;

import com.sportlink.notificationservice.model.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeviceTokenResponse {
    private UUID id;
    private String token;
    private Platform platform;
    private LocalDateTime registeredAt;
}
