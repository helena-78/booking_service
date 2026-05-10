package com.sportlink.notificationservice.controller;

import com.sportlink.notificationservice.dto.request.DispatchNotificationRequest;
import com.sportlink.notificationservice.dto.response.NotificationResponse;
import com.sportlink.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Inbox, dispatch, and mark-as-read operations")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "[Internal] Dispatch a notification",
            description = "Called by other services (Interaction, Activity, Booking) to send a notification. Not for direct client use.")
    @ApiResponse(responseCode = "201", description = "Notification dispatched")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse dispatch(@Valid @RequestBody DispatchNotificationRequest request) {
        return notificationService.dispatch(request);
    }

    @Operation(summary = "Get authenticated user's notification inbox",
            description = "Returns paginated notifications sorted newest-first. Use ?page=0&size=20.")
    @ApiResponse(responseCode = "200", description = "Paginated notification list")
    @GetMapping
    public Page<NotificationResponse> getInbox(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        return notificationService.getInbox(currentUserId, pageable);
    }

    @Operation(summary = "Mark a single notification as read")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found or does not belong to caller")
    })
    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID notificationId) {
        return notificationService.markAsRead(currentUserId, notificationId);
    }

    @Operation(summary = "Mark all in-app notifications as read for the authenticated user")
    @ApiResponse(responseCode = "204", description = "All notifications marked as read")
    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(@RequestHeader("X-User-Id") UUID currentUserId) {
        notificationService.markAllAsRead(currentUserId);
    }
}
