package com.sportlink.search.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
