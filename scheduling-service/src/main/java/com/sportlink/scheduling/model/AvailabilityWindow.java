package com.sportlink.scheduling.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "availability_windows")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityWindow {

    @Id
    @Column(name = "window_id")
    private UUID windowId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "time_range_from", nullable = false)
    private LocalDateTime timeRangeFrom;

    @Column(name = "time_range_to", nullable = false)
    private LocalDateTime timeRangeTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 3)
    private DayOfWeek dayOfWeek;
}