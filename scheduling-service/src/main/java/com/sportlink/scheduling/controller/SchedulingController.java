package com.sportlink.scheduling.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sportlink.scheduling.dto.AvailabilityWindowDto;
import com.sportlink.scheduling.dto.ReserveTimeSlotRequest;
import com.sportlink.scheduling.dto.ResolveTimeSlotsRequest;
import com.sportlink.scheduling.dto.TimeSlotDto;
import com.sportlink.scheduling.dto.UpdateAvailabilityRequest;
import com.sportlink.scheduling.service.SchedulingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * REST endpoints for the Scheduling Service.
 * Endpoint set matches Assignment #3 § "Scheduling Service".
 *
 * Per Project Guidelines § 3.2 — controllers expose endpoints, validate
 * input, and return responses; they hold no business logic.
 */


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/timeslots")
@Tag(name = "Scheduling", description = "Time slots and availability windows")
@RequiredArgsConstructor
public class SchedulingController {

    private final SchedulingService schedulingService;

    // ============================================================
    //  TimeSlots
    // ============================================================

    @GetMapping("/available")
    @Operation(summary = "List available time slots in a date range")
    public List<TimeSlotDto> findAvailable(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return schedulingService.findAvailableSlots(from, to);
    }

    @GetMapping
    @Operation(summary = "List all time slots regardless of status")
    public List<TimeSlotDto> findAll() {
        return schedulingService.findAllSlots();
    }

    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reserve a time slot",
            description = "Creates a new slot in RESERVED status. "
                    + "Used by Activity Management and Booking services.")
    public TimeSlotDto reserve(@Valid @RequestBody ReserveTimeSlotRequest request) {
        return schedulingService.reserveTimeSlot(request);
    }

    @DeleteMapping("/{slotId}/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Release a reserved time slot")
    public void release(@PathVariable UUID slotId) {
        schedulingService.releaseTimeSlot(slotId);
    }

    // ============================================================
    //  User availability
    // ============================================================

    @GetMapping("/users/{userId}/availability")
    @Operation(summary = "Get a user's recurring availability windows")
    public List<AvailabilityWindowDto> getUserAvailability(@PathVariable UUID userId) {
        return schedulingService.getUserAvailability(userId);
    }

    @PutMapping("/users/{userId}/availability")
    @Operation(summary = "Replace a user's availability schedule with the new list")
    public List<AvailabilityWindowDto> updateUserAvailability(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        return schedulingService.updateUserAvailability(userId, request);
    }

    @PostMapping("/resolve")
    @Operation(summary = "Find overlapping availability of multiple users",
            description = "Used by Matching Service to suggest meeting times "
                    + "for compatible users.")
    public List<AvailabilityWindowDto> resolve(
            @Valid @RequestBody ResolveTimeSlotsRequest request) {
        return schedulingService.resolveTimeSlots(request);
    }
}