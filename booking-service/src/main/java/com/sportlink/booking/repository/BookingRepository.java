package com.sportlink.booking.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.sportlink.booking.model.Booking;
import com.sportlink.booking.model.BookingStatus;

@Repository
public interface BookingRepository extends CrudRepository<Booking, String> {

    List<Booking> findByActivityId(String activityId);

    List<Booking> findBySportCenterId(String sportCenterId);

    List<Booking> findByBookingStatus(BookingStatus bookingStatus);

    List<Booking> findByActivityIdAndSportCenterId(String activityId, String sportCenterId);

    List<Booking> findByActivityIdAndBookingStatus(String activityId, BookingStatus bookingStatus);

    List<Booking> findBySportCenterIdAndBookingStatus(String sportCenterId, BookingStatus bookingStatus);

    List<Booking> findByActivityIdAndSportCenterIdAndBookingStatus(
            String activityId, String sportCenterId, BookingStatus bookingStatus);
}
