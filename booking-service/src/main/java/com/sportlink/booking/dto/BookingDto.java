package com.sportlink.booking.dto;

import java.time.LocalDateTime;

import com.sportlink.booking.model.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {

    private String bookingId;
    private String sportCenterId;
    private String activityId;
    private String timeSlotId;
    private BookingStatus bookingStatus;
    private LocalDateTime createdAt;
    private PaymentDto payment;

    public BookingDto(String bookingId) {
        this.bookingId = bookingId;
    }
}
