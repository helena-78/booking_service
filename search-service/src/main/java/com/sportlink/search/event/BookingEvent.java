package com.sportlink.search.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Read-side mirror of the BookingEvent payload published by the Booking
 * Service on the 'booking-events' Kafka topic.
 *
 * The class is intentionally not shared via a common Maven module: each
 * service owns its own copy of the contract. The shared agreement is the
 * JSON shape, not the Java class - this is what lets the two services be
 * independently deployable.
 *
 * Fields are deserialized by name from JSON, so they must match the
 * producer's field names exactly. {@link #eventType} is a String here
 * (rather than a copy of the producer's enum) so this consumer never
 * crashes if Booking Service adds a new event type in the future.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {

    /**
     * One of: BOOKING_CREATED, BOOKING_CONFIRMED, BOOKING_CANCELLED,
     * PAYMENT_REFUNDED. Kept as String so an unknown future value does
     * not break deserialization - the consumer simply ignores it.
     */
    private String eventType;

    private String bookingId;
    private String sportCenterId;
    private String activityId;
    private String timeSlotId;
    private String bookingStatus;
    private String paymentStatus;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime occurredAt;
}
