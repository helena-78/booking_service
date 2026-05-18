package com.sportlink.booking.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Envelope for every event the Booking Service publishes to Kafka.
 *
 * The same shape is reused across:
 *   - BookingCreated     (eventType = BOOKING_CREATED)
 *   - BookingConfirmed   (eventType = BOOKING_CONFIRMED)
 *   - BookingCancelled   (eventType = BOOKING_CANCELLED)
 *   - PaymentRefunded    (eventType = PAYMENT_REFUNDED)
 *
 * All events go to a single topic ('booking-events'). Consumers route on
 * the {@link #eventType} field (the Kafka record key is also set to it,
 * so log-compaction / partitioning by event type is possible later).
 *
 * Unused fields stay null and are dropped from the JSON payload thanks to
 * {@code @JsonInclude(NON_NULL)}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingEvent {

    /** What kind of event this is - drives consumer routing. */
    private BookingEventType eventType;

    /** The booking that produced the event. */
    private String bookingId;

    /** Logical reference to the sport center (owned by Account Mgmt). */
    private String sportCenterId;

    /** Logical reference to the activity (owned by Activity Mgmt). */
    private String activityId;

    /** Logical reference to the time slot (owned by Scheduling). */
    private String timeSlotId;

    /** Booking status at the moment the event was emitted. */
    private String bookingStatus;

    /** Payment status at the moment the event was emitted. */
    private String paymentStatus;

    /** Payment amount (only set on BookingCreated / PaymentRefunded). */
    private BigDecimal amount;

    /** Currency (e.g. EUR). */
    private String currency;

    /** When the event happened on the producer side. */
    private LocalDateTime occurredAt;
}
