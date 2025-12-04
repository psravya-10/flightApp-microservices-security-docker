package com.flightapp.booking.service;

import com.flightapp.booking.dto.*;
import com.flightapp.booking.entity.Booking;
import com.flightapp.booking.entity.Passenger;
import com.flightapp.booking.exception.BadRequestException;
import com.flightapp.booking.repository.BookingRepository;
import com.flightapp.booking.feign.FlightClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private FlightClient flightClient;

    @InjectMocks
    private BookingServiceImpl bookingService;  

    private Booking booking;
    private FlightSearchResponse flight;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        booking = new Booking();
        booking.setPnr("PNR001");
        booking.setCustomerName("Sravya");
        booking.setEmail("sravya@gmail.com");
        booking.setJourneyDate(LocalDate.of(2025, 12, 10));
        booking.setNumberOfSeats(2);
        booking.setSeatNumbers("12A,12B");
        booking.setMealPreference("VEG");
        booking.setFlightId("F001");
        booking.setBookingTime(LocalDateTime.now());
        booking.setPassengers(List.of(
                new Passenger("Sravya", "F", 21),
                new Passenger("Anu", "F", 22)
        ));

        flight = new FlightSearchResponse();
        flight.setFlightId("F001");
        flight.setAirlineName("Indigo");
        flight.setFromPlace("Hyderabad");
        flight.setToPlace("Bangalore");
        flight.setTripType("ONE-WAY");
        flight.setDepartureTime(LocalDateTime.of(2025, 12, 10, 10, 0));
        flight.setArrivalTime(LocalDateTime.of(2025, 12, 10, 12, 0));
        flight.setAvailableSeats(120);
    }

   
    @Test
    void testGetTicketByPnr() {
        when(bookingRepo.findByPnr("PNR001")).thenReturn(Optional.of(booking));
        when(flightClient.getFlightById("F001")).thenReturn(flight);

        TicketResponse response = bookingService.getTicketByPnr("PNR001");

        assertEquals("PNR001", response.getPnr());
        assertEquals("Indigo", response.getAirlineName());
        verify(bookingRepo, times(1)).findByPnr("PNR001");
        verify(flightClient, times(1)).getFlightById("F001");
    }

    
    @Test
    void testBookTicketSuccess() {
        BookingRequest req = new BookingRequest();
        req.setCustomerName("Sravya");
        req.setEmail("sravya@gmail.com");
        req.setJourneyDate(LocalDate.of(2025, 12, 10));
        req.setNumberOfSeats(2);
        req.setSeatNumbers("12A,12B");
        req.setMealPreference("VEG");
        req.setPassengers(List.of(
                new PassengerDto("Sravya", "F", 21),
                new PassengerDto("Anu", "F", 22)
        ));

        when(flightClient.getFlightById("F001")).thenReturn(flight);
        when(bookingRepo.save(any())).thenReturn(booking);

        TicketResponse response = bookingService.bookTicket("F001", req);

        assertEquals("Sravya", response.getCustomerName());
        assertEquals("Indigo", response.getAirlineName());
        verify(flightClient, times(1)).reserveSeats(eq("F001"), any());
        verify(bookingRepo, times(1)).save(any());
    }

    
    @Test
    void testBookTicketWrongDate() {
        BookingRequest req = new BookingRequest();
        req.setJourneyDate(LocalDate.of(2025, 12, 11)); 
        req.setNumberOfSeats(1);
        req.setPassengers(List.of(new PassengerDto("Sravya", "F", 21)));

        when(flightClient.getFlightById("F001")).thenReturn(flight);

        assertThrows(BadRequestException.class,
                () -> bookingService.bookTicket("F001", req));
    }
}
