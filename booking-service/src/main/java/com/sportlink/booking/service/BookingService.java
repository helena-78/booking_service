package com.sportlink.booking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sportlink.booking.client.SchedulingClient;
import com.sportlink.booking.client.SearchClient;
import com.sportlink.booking.dto.BookingDto;
import com.sportlink.booking.dto.CreateBookingRequest;
import com.sportlink.booking.dto.FacilityDto;
import com.sportlink.booking.dto.MoneyDto;
import com.sportlink.booking.dto.PaymentDto;
import com.sportlink.booking.dto.PaymentRequestDto;
import com.sportlink.booking.event.publisher.BookingEventPublisher;
import com.sportlink.booking.exception.BookingNotFoundException;
import com.sportlink.booking.exception.InvalidBookingStateException;
import com.sportlink.booking.model.Booking;
import com.sportlink.booking.model.BookingStatus;
import com.sportlink.booking.model.Money;
import com.sportlink.booking.model.Payment;
import com.sportlink.booking.model.PaymentStatus;
import com.sportlink.booking.repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SchedulingClient schedulingClient;
    private final SearchClient searchClient;
    private final BookingEventPublisher eventPublisher;

    public BookingDto createBooking(CreateBookingRequest request) {
        // Reserve the time slot via Scheduling Service.
        //    Throws SchedulingServiceException -> 503 if unreachable.
        schedulingClient.reserveTimeSlot(request.getSportCenterId(), request.getTimeSlotId());

        PaymentRequestDto pr = request.getPayment();
        Money money = Money.builder()
                .amount(pr != null ? pr.getAmount() : null)
                .currency(pr != null ? pr.getCurrency() : null)
                .build();

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .paymentMethod(pr != null ? pr.getPaymentMethod() : null)
                .paymentStatus(PaymentStatus.PENDING)
                .money(money)
                .build();

        Booking booking = Booking.builder()
                .bookingId(UUID.randomUUID().toString())
                .sportCenterId(request.getSportCenterId())
                .activityId(request.getActivityId())
                .timeSlotId(request.getTimeSlotId())
                .bookingStatus(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .payment(payment)
                .build();

        bookingRepository.save(booking);
        log.info("Booking {} created with status PENDING", booking.getBookingId());

        // Publish BookingCreated event asynchronously (Kafka).
        // Consumed by: Notification
        eventPublisher.publishBookingCreated(booking);

        return mapToBookingDto(booking);
    }

    public BookingDto getBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking " + bookingId + " not found"));
        return mapToBookingDto(booking);
    }

    public List<BookingDto> listBookings(String activityId, String sportCenterId, BookingStatus bookingStatus) {
        List<Booking> bookings;

        boolean hasActivity = activityId != null && !activityId.isBlank();
        boolean hasCenter = sportCenterId != null && !sportCenterId.isBlank();
        boolean hasStatus = bookingStatus != null;

        if (hasActivity && hasCenter && hasStatus) {
            bookings = bookingRepository.findByActivityIdAndSportCenterIdAndBookingStatus(
                    activityId, sportCenterId, bookingStatus);
        } else if (hasActivity && hasCenter) {
            bookings = bookingRepository.findByActivityIdAndSportCenterId(activityId, sportCenterId);
        } else if (hasActivity && hasStatus) {
            bookings = bookingRepository.findByActivityIdAndBookingStatus(activityId, bookingStatus);
        } else if (hasCenter && hasStatus) {
            bookings = bookingRepository.findBySportCenterIdAndBookingStatus(sportCenterId, bookingStatus);
        } else if (hasActivity) {
            bookings = bookingRepository.findByActivityId(activityId);
        } else if (hasCenter) {
            bookings = bookingRepository.findBySportCenterId(sportCenterId);
        } else if (hasStatus) {
            bookings = bookingRepository.findByBookingStatus(bookingStatus);
        } else {
            bookings = new ArrayList<>();
            bookingRepository.findAll().forEach(bookings::add);
        }

        return bookings.stream().map(this::mapToBookingDto).toList();
    }

    public BookingDto confirmBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking " + bookingId + " not found"));

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingStateException(
                    "Cannot confirm booking with status " + booking.getBookingStatus());
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        if (booking.getPayment() != null) {
            booking.getPayment().setPaymentStatus(PaymentStatus.PAID);
            booking.getPayment().setPaidAt(LocalDateTime.now());
        }
        bookingRepository.save(booking);
        log.info("Booking {} confirmed and payment marked as PAID", bookingId);

        // Publish BookingConfirmed event asynchronously (Kafka).
        // Consumed by: Notification, Search, Scheduling 
        eventPublisher.publishBookingConfirmed(booking);

        return mapToBookingDto(booking);
    }

    public BookingDto cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking " + bookingId + " not found"));

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Booking is already cancelled");
        }

        // Release the slot via Scheduling Service.
        // Throws SchedulingServiceException -> 503 if unreachable.
        schedulingClient.releaseTimeSlot(booking.getTimeSlotId());

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking {} cancelled", bookingId);

        // Publish BookingCancelled event asynchronously (Kafka).
        // Consumed by: Search 
        eventPublisher.publishBookingCancelled(booking);

        return mapToBookingDto(booking);
    }

    public BookingDto refundBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking " + bookingId + " not found"));

        if (booking.getBookingStatus() != BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException(
                    "Refund only valid when bookingStatus is CANCELLED");
        }
        if (booking.getPayment() == null
                || booking.getPayment().getPaymentStatus() != PaymentStatus.PAID) {
            throw new InvalidBookingStateException(
                    "Refund only valid when paymentStatus is PAID");
        }

        booking.getPayment().setPaymentStatus(PaymentStatus.REFUNDED);
        booking.getPayment().setRefundedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        log.info("Booking {} refunded", bookingId);

        // Publish PaymentRefunded event asynchronously (Kafka).
        // Consumed by: Notification 
        eventPublisher.publishPaymentRefunded(booking);

        return mapToBookingDto(booking);
    }

    public List<FacilityDto> searchFacilities(String sportType, String location, String timeSlotId) {
        return searchClient.searchFacilities(sportType, location, timeSlotId);
    }

    private BookingDto mapToBookingDto(Booking booking) {
        return BookingDto.builder()
                .bookingId(booking.getBookingId())
                .sportCenterId(booking.getSportCenterId())
                .activityId(booking.getActivityId())
                .timeSlotId(booking.getTimeSlotId())
                .bookingStatus(booking.getBookingStatus())
                .createdAt(booking.getCreatedAt())
                .payment(mapToPaymentDto(booking.getPayment()))
                .build();
    }

    private PaymentDto mapToPaymentDto(Payment payment) {
        if (payment == null) {
            return null;
        }
        MoneyDto moneyDto = null;
        if (payment.getMoney() != null) {
            moneyDto = MoneyDto.builder()
                    .amount(payment.getMoney().getAmount())
                    .currency(payment.getMoney().getCurrency())
                    .build();
        }
        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paidAt(payment.getPaidAt())
                .refundedAt(payment.getRefundedAt())
                .money(moneyDto)
                .build();
    }
}
