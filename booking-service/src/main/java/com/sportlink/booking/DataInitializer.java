package com.sportlink.booking;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sportlink.booking.model.Booking;
import com.sportlink.booking.model.BookingStatus;
import com.sportlink.booking.model.Money;
import com.sportlink.booking.model.Payment;
import com.sportlink.booking.model.PaymentStatus;
import com.sportlink.booking.repository.BookingRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadBookingsData(BookingRepository bookingRepository) {
        return args -> {

            // Sample 1: PENDING booking
            Money money1 = Money.builder()
                    .amount(BigDecimal.valueOf(50.00))
                    .currency("EUR")
                    .build();

            Payment payment1 = Payment.builder()
                    .paymentId("pay-001")
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.PENDING)
                    .money(money1)
                    .build();

            Booking booking1 = Booking.builder()
                    .bookingId("bk-001")
                    .sportCenterId("sc-100")
                    .activityId("act-200")
                    .timeSlotId("ts-300")
                    .bookingStatus(BookingStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .payment(payment1)
                    .build();

            bookingRepository.save(booking1);

            // Sample 2: CONFIRMED booking
            Money money2 = Money.builder()
                    .amount(BigDecimal.valueOf(75.50))
                    .currency("EUR")
                    .build();

            Payment payment2 = Payment.builder()
                    .paymentId("pay-002")
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.PAID)
                    .paidAt(LocalDateTime.now())
                    .money(money2)
                    .build();

            Booking booking2 = Booking.builder()
                    .bookingId("bk-002")
                    .sportCenterId("sc-101")
                    .activityId("act-201")
                    .timeSlotId("ts-301")
                    .bookingStatus(BookingStatus.CONFIRMED)
                    .createdAt(LocalDateTime.now())
                    .payment(payment2)
                    .build();

            bookingRepository.save(booking2);
        };
    }
}
