package com.flightapp.booking.repository;

import com.flightapp.booking.entity.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {
    Optional<Booking> findByPnr(String pnr);
    List<Booking> findByEmailOrderByBookingTimeDesc(String email);
    List<Booking> findByFlightIdAndCancelledFalse(String flightId);

}
