package com.sportlink.booking.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
@Table(name = "bookings")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    @Id
    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "sport_center_id")
    private String sportCenterId;

    @Column(name = "activity_id")
    private String activityId;

    @Column(name = "time_slot_id")
    private String timeSlotId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus bookingStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Embedded
    private Payment payment;

    public Booking(String bookingId) {
        this.bookingId = bookingId;
    }
}
