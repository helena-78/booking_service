package com.sportlink.booking.model;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Money {
    private BigDecimal amount;
    private String currency;
}
