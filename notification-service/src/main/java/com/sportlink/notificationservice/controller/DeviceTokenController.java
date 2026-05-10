package com.sportlink.notificationservice.controller;

import com.sportlink.notificationservice.dto.request.DeviceTokenRequest;
import com.sportlink.notificationservice.dto.response.DeviceTokenResponse;
import com.sportlink.notificationservice.service.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/devices")
@RequiredArgsConstructor
@Tag(name = "Device Tokens", description = "Register and deregister push notification device tokens")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @Operation(summary = "Register a device push token for the authenticated user")
    @ApiResponse(responseCode = "201", description = "Device token registered")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceTokenResponse register(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @Valid @RequestBody DeviceTokenRequest request) {
        return deviceTokenService.register(currentUserId, request);
    }

    @Operation(summary = "Deregister a device token (on logout or app uninstall)")
    @ApiResponse(responseCode = "204", description = "Device token removed")
    @DeleteMapping("/{deviceToken}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deregister(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable String deviceToken) {
        deviceTokenService.deregister(currentUserId, deviceToken);
    }
}
