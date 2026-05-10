package com.sportlink.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/bookings.
 * Per Assignment 3: accepts sportCenterId, activityId, timeSlotId, and payment details.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateBookingRequest {
    private String sportCenterId;
    private String activityId;
    private String timeSlotId;
    private PaymentRequestDto payment;
}
