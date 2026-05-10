package com.sportlink.activitymanagement.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    private UUID userId;
    private String name;
    private String status; // ACTIVE / SUSPENDED / BANNED
    private String role;   // USER / ORGANIZER / MODERATOR
}