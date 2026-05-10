package com.sportlink.booking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sportlink.booking.dto.BookingDto;
import com.sportlink.booking.dto.CreateBookingRequest;
import com.sportlink.booking.dto.FacilityDto;
import com.sportlink.booking.model.BookingStatus;
import com.sportlink.booking.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto createBooking(@RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/bookings/{bookingId}")
    public BookingDto getBooking(@PathVariable String bookingId) {
        return bookingService.getBooking(bookingId);
    }

    @GetMapping("/bookings")
    public List<BookingDto> listBookings(
            @RequestParam(required = false) String activityId,
            @RequestParam(required = false) String sportCenterId,
            @RequestParam(required = false) BookingStatus bookingStatus) {
        return bookingService.listBookings(activityId, sportCenterId, bookingStatus);
    }

    @PutMapping("/bookings/{bookingId}/confirm")
    public BookingDto confirmBooking(@PathVariable String bookingId) {
        return bookingService.confirmBooking(bookingId);
    }

    @PutMapping("/bookings/{bookingId}/cancel")
    public BookingDto cancelBooking(@PathVariable String bookingId) {
        return bookingService.cancelBooking(bookingId);
    }

    @PostMapping("/bookings/{bookingId}/refund")
    public BookingDto refundBooking(@PathVariable String bookingId) {
        return bookingService.refundBooking(bookingId);
    }

    @GetMapping("/bookings/facilities/search")
    public List<FacilityDto> searchFacilities(
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String timeSlotId) {
        return bookingService.searchFacilities(sportType, location, timeSlotId);
    }
}
