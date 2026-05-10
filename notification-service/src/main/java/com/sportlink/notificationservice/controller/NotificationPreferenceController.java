package com.sportlink.notificationservice.controller;

import com.sportlink.notificationservice.dto.request.PreferenceUpdateRequest;
import com.sportlink.notificationservice.dto.response.PreferenceResponse;
import com.sportlink.notificationservice.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
@Tag(name = "Notification Preferences", description = "Enable or disable notification types per channel")
public class NotificationPreferenceController {

    private final PreferenceService preferenceService;

    @Operation(summary = "Get current user's notification preferences")
    @GetMapping
    public List<PreferenceResponse> getPreferences(@RequestHeader("X-User-Id") UUID currentUserId) {
        return preferenceService.getPreferences(currentUserId);
    }

    @Operation(summary = "Update a notification preference",
            description = "Enable or disable a specific event type on a specific channel.")
    @PatchMapping
    public PreferenceResponse updatePreference(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @Valid @RequestBody PreferenceUpdateRequest request) {
        return preferenceService.updatePreference(currentUserId, request);
    }
}
