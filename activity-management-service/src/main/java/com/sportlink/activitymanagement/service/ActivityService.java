package com.sportlink.activitymanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportlink.activitymanagement.client.AccountManagementClient;
import com.sportlink.activitymanagement.dto.ActivityDto;
import com.sportlink.activitymanagement.dto.CreateActivityRequest;
import com.sportlink.activitymanagement.dto.ParticipantDto;
import com.sportlink.activitymanagement.dto.UpdateActivityRequest;
import com.sportlink.activitymanagement.exception.ActivityFullException;
import com.sportlink.activitymanagement.exception.ActivityNotFoundException;
import com.sportlink.activitymanagement.exception.ParticipantNotFoundException;
import com.sportlink.activitymanagement.model.Activity;
import com.sportlink.activitymanagement.model.ActivityStatus;
import com.sportlink.activitymanagement.model.Participant;
import com.sportlink.activitymanagement.model.ParticipantRole;
import com.sportlink.activitymanagement.repository.ActivityRepository;
import com.sportlink.activitymanagement.repository.ParticipantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ParticipantRepository participantRepository;
    private final AccountManagementClient accountManagementClient;
    private final com.sportlink.activitymanagement.client.SchedulingClient schedulingClient;
    private final ActivityEventPublisher activityEventPublisher;


    @Transactional
    public ActivityDto createActivity(CreateActivityRequest request) {
        accountManagementClient.validateActiveUser(request.getOrganizerId());

        Activity activity = Activity.builder()
                .activityId(UUID.randomUUID())
                .organizerId(request.getOrganizerId())
                .title(request.getTitle())
                .sportType(request.getSportType())
                .status(ActivityStatus.PLANNED)
                .maxParticipants(request.getMaxParticipants())
                .description(request.getDescription())
                .preferredTimeSlotId(request.getPreferredTimeSlotId())
                .facilityBookingId(request.getFacilityBookingId())
                .build();

        // The organizer is automatically the first participant.
        Participant organizerAsParticipant = Participant.builder()
                .participantId(UUID.randomUUID())
                .userId(request.getOrganizerId())
                .activity(activity)
                .joinedAt(LocalDateTime.now())
                .role(ParticipantRole.ORGANIZER)
                .build();
        activity.getParticipants().add(organizerAsParticipant);

        Activity saved = activityRepository.save(activity);
        // If a time slot was requested, reserve it via Scheduling Service.
        // if Scheduling is down, the activity is still created.
        if (request.getPreferredTimeSlotId() != null) {
            // Use the preferred slot ID as the start anchor; default 1-hour duration.
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            UUID reservedSlotId = schedulingClient.reserveSlot(
                    now, now.plusHours(1),
                    saved.getOrganizerId(), saved.getActivityId());
            if (reservedSlotId != null) {
                saved.setPreferredTimeSlotId(reservedSlotId);
                activityRepository.save(saved);
            }
        }

        log.info("Activity {} created by organizer {}", saved.getActivityId(), saved.getOrganizerId());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public ActivityDto getActivity(UUID activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException(
                        "Activity " + activityId + " not found"));
        return toDto(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> getAllActivities() {
        return activityRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public ActivityDto updateActivity(UUID activityId, UpdateActivityRequest request) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException(
                        "Activity " + activityId + " not found"));

        if (activity.getStatus() == ActivityStatus.CANCELLED
                || activity.getStatus() == ActivityStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot update activity in status " + activity.getStatus());
        }

        if (request.getTitle() != null) {
            activity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            activity.setDescription(request.getDescription());
        }
        if (request.getPreferredTimeSlotId() != null) {
            activity.setPreferredTimeSlotId(request.getPreferredTimeSlotId());
        }
        if (request.getMaxParticipants() != null) {
            if (request.getMaxParticipants() < activity.getParticipants().size()) {
                throw new IllegalStateException(
                        "Cannot reduce maxParticipants below current participant count ("
                        + activity.getParticipants().size() + ")");
            }
            activity.setMaxParticipants(request.getMaxParticipants());
        }

        Activity saved = activityRepository.save(activity);
        log.info("Activity {} updated", activityId);
        return toDto(saved);
    }

    @Transactional
        public void cancelActivity(UUID activityId) {
            Activity activity = activityRepository.findById(activityId)
                    .orElseThrow(() -> new ActivityNotFoundException(
                            "Activity " + activityId + " not found"));

            if (activity.getStatus() == ActivityStatus.CANCELLED) {
                return;
            }
            if (activity.getStatus() == ActivityStatus.COMPLETED) {
                throw new IllegalStateException("Cannot cancel a completed activity");
            }

            activity.setStatus(ActivityStatus.CANCELLED);
            activityRepository.save(activity);

            // Publish ActivityCancelledEvent to Kafka - Scheduling Service consumes
            // it and releases the reserved time slot asynchronously
            com.sportlink.activitymanagement.event.ActivityCancelledEvent event =
                    com.sportlink.activitymanagement.event.ActivityCancelledEvent.builder()
                            .activityId(activity.getActivityId())
                            .preferredTimeSlotId(activity.getPreferredTimeSlotId())
                            .cancelledAt(java.time.LocalDateTime.now())
                            .build();
            activityEventPublisher.publishActivityCancelled(event);

            log.info("Activity {} cancelled", activityId);
        }

    // @Transactional
    // public void cancelActivity(UUID activityId) {
    //     Activity activity = activityRepository.findById(activityId)
    //             .orElseThrow(() -> new ActivityNotFoundException(
    //                     "Activity " + activityId + " not found"));

    //     if (activity.getStatus() == ActivityStatus.CANCELLED) {
    //         return; 
    //     }
    //     if (activity.getStatus() == ActivityStatus.COMPLETED) {
    //         throw new IllegalStateException("Cannot cancel a completed activity");
    //     }

    //     activity.setStatus(ActivityStatus.CANCELLED);
    //     activityRepository.save(activity);
    //     // Release the reserved time slot in Scheduling Service.
    //     if (activity.getPreferredTimeSlotId() != null) {
    //         schedulingClient.releaseSlot(activity.getPreferredTimeSlotId());
    //     }
    //     log.info("Activity {} cancelled", activityId);
    // }
    

   
    //  Participants
    
    @Transactional
    public ParticipantDto joinActivity(UUID activityId, UUID userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException(
                        "Activity " + activityId + " not found"));

        if (activity.getStatus() != ActivityStatus.PLANNED
                && activity.getStatus() != ActivityStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot join activity in status " + activity.getStatus());
        }

        // Sync call to Account Management to validate the joining user.
        accountManagementClient.validateActiveUser(userId);

        // Already participant?
        boolean alreadyJoined = activity.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));
        if (alreadyJoined) {
            throw new IllegalStateException(
                    "User " + userId + " already joined activity " + activityId);
        }

        // Capacity check
        if (activity.getParticipants().size() >= activity.getMaxParticipants()) {
            throw new ActivityFullException("Activity " + activityId + " is full");
        }

        Participant participant = Participant.builder()
                .participantId(UUID.randomUUID())
                .userId(userId)
                .activity(activity)
                .joinedAt(LocalDateTime.now())
                .role(ParticipantRole.PLAYER)
                .build();

        activity.getParticipants().add(participant);
        activityRepository.save(activity);
        log.info("User {} joined activity {}", userId, activityId);
        return toParticipantDto(participant);
    }

    @Transactional
    public void leaveActivity(UUID activityId, UUID userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException(
                        "Activity " + activityId + " not found"));

        Participant participant = participantRepository
                .findByActivity_ActivityIdAndUserId(activityId, userId)
                .orElseThrow(() -> new ParticipantNotFoundException(
                        "User " + userId + " is not a participant of activity " + activityId));

        if (participant.getRole() == ParticipantRole.ORGANIZER) {
            throw new IllegalStateException(
                    "Organizer cannot leave their own activity — cancel it instead");
        }

        activity.getParticipants().remove(participant);
        participantRepository.delete(participant);
        log.info("User {} left activity {}", userId, activityId);
    }

    @Transactional(readOnly = true)
    public List<ParticipantDto> getParticipants(UUID activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException(
                        "Activity " + activityId + " not found"));
        return activity.getParticipants().stream().map(this::toParticipantDto).toList();
    }

    /**
     * Organizer removes a participant from the activity
     */
    @Transactional
    public void removeParticipant(UUID activityId, UUID userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException(
                        "Activity " + activityId + " not found"));

        Participant participant = participantRepository
                .findByActivity_ActivityIdAndUserId(activityId, userId)
                .orElseThrow(() -> new ParticipantNotFoundException(
                        "User " + userId + " is not a participant of activity " + activityId));

        if (participant.getRole() == ParticipantRole.ORGANIZER) {
            throw new IllegalStateException("Cannot remove the organizer");
        }

        activity.getParticipants().remove(participant);
        participantRepository.delete(participant);
        log.info("Organizer removed user {} from activity {}", userId, activityId);
    }

   
    //  Mappers (entity <-> DTO)
    

    private ActivityDto toDto(Activity a) {
        return ActivityDto.builder()
                .activityId(a.getActivityId())
                .organizerId(a.getOrganizerId())
                .title(a.getTitle())
                .sportType(a.getSportType())
                .status(a.getStatus())
                .maxParticipants(a.getMaxParticipants())
                .preferredTimeSlotId(a.getPreferredTimeSlotId())
                .facilityBookingId(a.getFacilityBookingId())
                .description(a.getDescription())
                .participants(a.getParticipants().stream().map(this::toParticipantDto).toList())
                .build();
    }

    private ParticipantDto toParticipantDto(Participant p) {
        return ParticipantDto.builder()
                .participantId(p.getParticipantId())
                .userId(p.getUserId())
                .joinedAt(p.getJoinedAt())
                .role(p.getRole())
                .build();
    }
}