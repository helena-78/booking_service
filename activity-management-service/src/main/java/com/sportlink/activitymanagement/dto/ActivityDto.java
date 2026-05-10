package com.sportlink.activitymanagement.dto;

import java.util.List;
import java.util.UUID;

import com.sportlink.activitymanagement.model.ActivityStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDto {

    private UUID activityId;
    private UUID organizerId;
    private String title;
    private String sportType;
    private ActivityStatus status;
    private Integer maxParticipants;
    private UUID preferredTimeSlotId;
    private UUID facilityBookingId;
    private String description;

    private List<ParticipantDto> participants;
}