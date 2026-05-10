package com.sportlink.booking.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment details supplied by the client when creating a booking.
 * Maps to: amount, currency, paymentMethod (per Assignment 3).
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDto {
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
}
