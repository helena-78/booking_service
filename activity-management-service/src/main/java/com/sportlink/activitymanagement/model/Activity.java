package com.sportlink.activitymanagement.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "activities")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Activity {

    @Id
    @Column(name = "activity_id")
    private UUID activityId;

    @Column(name = "organizer_id", nullable = false)
    private UUID organizerId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "sport_type", nullable = false, length = 50)
    private String sportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ActivityStatus status;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "preferred_time_slot_id")
    private UUID preferredTimeSlotId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "facility_booking_id")
    private UUID facilityBookingId;

    @OneToMany(mappedBy = "activity",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<Participant> participants = new ArrayList<>();
}