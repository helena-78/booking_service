package com.sportlink.activitymanagement.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sportlink.activitymanagement.dto.ActivityDto;
import com.sportlink.activitymanagement.dto.CreateActivityRequest;
import com.sportlink.activitymanagement.dto.ParticipantDto;
import com.sportlink.activitymanagement.dto.UpdateActivityRequest;
import com.sportlink.activitymanagement.service.ActivityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/activities")
@Tag(name = "Activities", description = "Activity lifecycle and participant management")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    //  Activities
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new activity",
            description = "Validates the organizer via Account Management Service "
                    + "and persists the activity in PLANNED state.")
    public ActivityDto createActivity(@Valid @RequestBody CreateActivityRequest request) {
        return activityService.createActivity(request);
    }

    @GetMapping
    @Operation(summary = "List all activities")
    public List<ActivityDto> getAllActivities() {
        return activityService.getAllActivities();
    }

    @GetMapping("/{activityId}")
    @Operation(summary = "Get activity by ID, including participants")
    public ActivityDto getActivity(@PathVariable UUID activityId) {
        return activityService.getActivity(activityId);
    }

    @PutMapping("/{activityId}")
    @Operation(summary = "Update activity (organizer only)")
    public ActivityDto updateActivity(@PathVariable UUID activityId,
                                      @Valid @RequestBody UpdateActivityRequest request) {
        return activityService.updateActivity(activityId, request);
    }

    @DeleteMapping("/{activityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel activity (sets status = CANCELLED)")
    public void cancelActivity(@PathVariable UUID activityId) {
        activityService.cancelActivity(activityId);
    }

    //  Participants

    @PostMapping("/{activityId}/join")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Join an activity",
            description = "Validates the user, checks capacity, then enrolls them.")
    public ParticipantDto joinActivity(@PathVariable UUID activityId,
                                       @RequestBody Map<String, UUID> body) {
        UUID userId = body.get("userId");
        return activityService.joinActivity(activityId, userId);
    }

    @PostMapping("/{activityId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Leave an activity")
    public void leaveActivity(@PathVariable UUID activityId,
                              @RequestBody Map<String, UUID> body) {
        UUID userId = body.get("userId");
        activityService.leaveActivity(activityId, userId);
    }

    @GetMapping("/{activityId}/participants")
    @Operation(summary = "List participants of an activity")
    public List<ParticipantDto> getParticipants(@PathVariable UUID activityId) {
        return activityService.getParticipants(activityId);
    }

    @DeleteMapping("/{activityId}/participants/{userId}")
    @Operation(summary = "Organizer removes a participant from the activity")
    public ResponseEntity<Void> removeParticipant(@PathVariable UUID activityId,
                                                  @PathVariable UUID userId) {
        activityService.removeParticipant(activityId, userId);
        return ResponseEntity.noContent().build();
    }
}