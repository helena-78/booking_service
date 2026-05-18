package com.sportlink.booking.event.publisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.sportlink.booking.event.BookingEvent;
import com.sportlink.booking.event.BookingEventType;
import com.sportlink.booking.model.Booking;
import com.sportlink.booking.model.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes Booking domain events to Kafka.
 *
 * Asynchronous boundary: BookingService persists the state change first
 * (synchronously, in the request thread) and then calls one of the
 * {@code publishXxx} methods below. KafkaTemplate.send() returns immediately;
 * the actual network write happens off the request thread. Failures are
 * logged but do NOT roll back the booking - the REST call must remain
 * successful for the caller.
 *
 * (Previously this responsibility was planned on RabbitMQ; we now use Kafka.)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisher {

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    @Value("${sportlink.kafka.topics.booking-events}")
    private String bookingEventsTopic;

    public void publishBookingCreated(Booking booking) {
        publish(buildEvent(BookingEventType.BOOKING_CREATED, booking));
    }

    public void publishBookingConfirmed(Booking booking) {
        publish(buildEvent(BookingEventType.BOOKING_CONFIRMED, booking));
    }

    public void publishBookingCancelled(Booking booking) {
        publish(buildEvent(BookingEventType.BOOKING_CANCELLED, booking));
    }

    public void publishPaymentRefunded(Booking booking) {
        publish(buildEvent(BookingEventType.PAYMENT_REFUNDED, booking));
    }

    // -----------------------------------------------------------------
    // internals
    // -----------------------------------------------------------------

    private BookingEvent buildEvent(BookingEventType type, Booking booking) {
        Payment payment = booking.getPayment();
        BigDecimal amount = null;
        String currency = null;
        String paymentStatus = null;

        if (payment != null) {
            paymentStatus = payment.getPaymentStatus() != null
                    ? payment.getPaymentStatus().name()
                    : null;
            if (payment.getMoney() != null) {
                amount = payment.getMoney().getAmount();
                currency = payment.getMoney().getCurrency();
            }
        }

        return BookingEvent.builder()
                .eventType(type)
                .bookingId(booking.getBookingId())
                .sportCenterId(booking.getSportCenterId())
                .activityId(booking.getActivityId())
                .timeSlotId(booking.getTimeSlotId())
                .bookingStatus(booking.getBookingStatus() != null
                        ? booking.getBookingStatus().name() : null)
                .paymentStatus(paymentStatus)
                .amount(amount)
                .currency(currency)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    private void publish(BookingEvent event) {
        // The key = event type, so all events of one type share a partition
        // (preserves per-type ordering without forcing a single partition for
        // the whole topic).
        String key = event.getEventType().name();

        CompletableFuture<SendResult<String, BookingEvent>> future =
                kafkaTemplate.send(bookingEventsTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                // Async failure: log but do NOT propagate - the HTTP response
                // to the user has already been (or is about to be) committed.
                log.error("Failed to publish {} event for booking {}: {}",
                        event.getEventType(), event.getBookingId(), ex.getMessage(), ex);
            } else {
                log.info("Published {} event for booking {} to topic '{}' partition {} offset {}",
                        event.getEventType(),
                        event.getBookingId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
