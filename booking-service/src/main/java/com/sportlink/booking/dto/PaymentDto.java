package com.sportlink.booking.dto;

import java.time.LocalDateTime;

import com.sportlink.booking.model.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private String paymentId;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private MoneyDto money;
}
