package com.sportlink.activitymanagement.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sportlink.activitymanagement.model.ParticipantRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDto {

    private UUID participantId;
    private UUID userId;
    private LocalDateTime joinedAt;
    private ParticipantRole role;
}