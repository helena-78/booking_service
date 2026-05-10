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
@Table(name = "time_slots")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {

    @Id
    @Column(name = "slot_id")
    private UUID slotId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TimeSlotStatus status;

    @Column(name = "organizer_id")
    private UUID organizerId;

    @Column(name = "reserved_for_id")
    private UUID reservedForId;
}