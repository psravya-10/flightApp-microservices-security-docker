package com.flightapp.flight.service;

import com.flightapp.flight.dto.*;
import com.flightapp.flight.entity.*;
import com.flightapp.flight.exception.BadRequestException;
import com.flightapp.flight.repository.AirlineRepository;
import com.flightapp.flight.repository.FlightRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepo;
    @Mock
    private AirlineRepository airlineRepo;

    @InjectMocks
    private FlightServiceImpl service;

    private Flight flight;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        flight = new Flight();
        flight.setId("F1");
        flight.setAirlineId("A1");
        flight.setFromPlace("Hyderabad");
        flight.setToPlace("Bangalore");
        flight.setDepartureTime(LocalDateTime.now());
        flight.setArrivalTime(LocalDateTime.now().plusHours(1));
        flight.setTripType("ONE-WAY");
        flight.setTotalSeats(120);
        flight.setAvailableSeats(120);
    }

    @Test
    void testGetFlightByIdSuccess() {
        when(flightRepo.findById("F1")).thenReturn(Optional.of(flight));

        FlightSearchResponse response = service.getFlightById("F1");

        assertEquals("F1", response.getFlightId());
        assertEquals("Hyderabad", response.getFromPlace());
    }

    @Test
    void testGetFlightByIdNotFound() {
        when(flightRepo.findById("F1")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getFlightById("F1"));
    }

    @Test
    void testReserveSeatsSuccess() {
        when(flightRepo.findById("F1")).thenReturn(Optional.of(flight));

        service.reserveSeats("F1", 10);

        assertEquals(110, flight.getAvailableSeats());
        verify(flightRepo).save(flight);
    }

    @Test
    void testReserveSeatsNotEnough() {
        when(flightRepo.findById("F1")).thenReturn(Optional.of(flight));
        assertThrows(BadRequestException.class, () -> service.reserveSeats("F1", 200));
    }

    @Test
    void testReleaseSeatsSuccess() {
        flight.setAvailableSeats(100);
        when(flightRepo.findById("F1")).thenReturn(Optional.of(flight));

        service.releaseSeats("F1", 10);

        assertEquals(110, flight.getAvailableSeats());
        verify(flightRepo).save(flight);
    }
}
