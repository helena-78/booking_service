package com.sportlink.search.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.sportlink.search.exception.BookingServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Synchronous client for the Booking Service.
 *
 * The Search Service uses this to confirm a facility actually has at least
 * one booking record in the Booking Service before exposing it in search
 * results filtered by sportCenterId.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.booking.url:http://localhost:8088}")
    private String bookingServiceUrl;

    /**
     * Returns the list of booking IDs that the Booking Service has on record
     * for the given sport center. Returns an empty list if the sport center
     * has no bookings.
     */
    public List<String> getBookingIdsForSportCenter(String sportCenterId) {
        if (sportCenterId == null || sportCenterId.isBlank()) {
            return new ArrayList<>();
        }
        String url = bookingServiceUrl + "/api/bookings?sportCenterId=" + sportCenterId;
        try {
            List<BookingSummary> bookings = webClientBuilder
                    .build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToFlux(BookingSummary.class)
                    .collectList()
                    .block();

            if (bookings == null) {
                return new ArrayList<>();
            }
            List<String> ids = new ArrayList<>(bookings.size());
            for (BookingSummary b : bookings) {
                if (b != null && b.bookingId() != null) {
                    ids.add(b.bookingId());
                }
            }
            return ids;
        } catch (Exception e) {
            log.error("Booking lookup via Booking Service failed: {}", e.getMessage());
            throw new BookingServiceException("Booking Service unavailable", e);
        }
    }

    public record BookingSummary(String bookingId) {}
}
