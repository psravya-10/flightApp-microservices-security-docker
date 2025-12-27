package com.flightapp.booking.service;

import com.flightapp.booking.dto.*;
import com.flightapp.booking.entity.*;
import com.flightapp.booking.exception.*;
import com.flightapp.booking.repository.BookingRepository;
import com.flightapp.booking.feign.FlightClient;
import com.flightapp.booking.util.PnrGenerator;
import com.flightapp.booking.service.BookingEventPublisher;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepo;
    private final FlightClient flightClient;
    private final BookingEventPublisher bookingEventPublisher;

    public BookingServiceImpl(BookingRepository bookingRepo,
                              FlightClient flightClient,
                              BookingEventPublisher bookingEventPublisher) {
        this.bookingRepo = bookingRepo;
        this.flightClient = flightClient;
        this.bookingEventPublisher = bookingEventPublisher;
    }


    @Override
    @CircuitBreaker(name = "flightCB", fallbackMethod = "flightServiceFallback")
    public TicketResponse bookTicket(String flightId, BookingRequest req) {

        FlightSearchResponse flight = flightClient.getFlightById(flightId);

        if (!flight.getDepartureTime().toLocalDate().equals(req.getJourneyDate()))
            throw new BadRequestException("Wrong journey date");

        if (req.getNumberOfSeats() != req.getPassengers().size())
            throw new BadRequestException("Passenger count must match seat count");

        flightClient.reserveSeats(flightId, new ReserveRequest(req.getNumberOfSeats()));

        Booking b = new Booking();
        b.setPnr(PnrGenerator.generate());
        b.setCustomerName(req.getCustomerName());
        b.setEmail(req.getEmail());
        b.setSeatNumbers(req.getSeatNumbers());
        b.setMealPreference(req.getMealPreference());
        b.setJourneyDate(req.getJourneyDate());
        b.setCancelled(false);
        b.setNumberOfSeats(req.getNumberOfSeats());
        b.setBookingTime(LocalDateTime.now());
        b.setFlightId(flightId);

        List<Passenger> list = req.getPassengers().stream()
                .map(p -> new Passenger(p.getName(), p.getGender(), p.getAge()))
                .collect(Collectors.toList());

        b.setPassengers(list);
        Booking saved = bookingRepo.save(b);

        // Publish booking confirmed event to RabbitMQ
        bookingEventPublisher.publishBookingConfirmed(saved);

        return toResponse(saved, flight);

    }
    public TicketResponse flightServiceFallback(String flightId, BookingRequest req, Throwable ex) {
        System.out.println("CIRCUIT BREAKER TRIGGERED: Flight Service DOWN");
        System.out.println(" Reason: " + ex.getMessage()); 
        TicketResponse r = new TicketResponse();
        r.setPnr("N/A");
        r.setCustomerName(req.getCustomerName());

        return r;
    }



    @Override
    public TicketResponse getTicketByPnr(String pnr) {
        Booking b = bookingRepo.findByPnr(pnr)
                .orElseThrow(() -> new NotFoundException("PNR not found"));

        FlightSearchResponse f = flightClient.getFlightById(b.getFlightId());
        return toResponse(b, f);
    }

    @Override
    public List<TicketResponse> getHistory(String email) {
        return bookingRepo.findByEmailOrderByBookingTimeDesc(email).stream().map(b -> {
            FlightSearchResponse f = flightClient.getFlightById(b.getFlightId());
            return toResponse(b, f);
        }).collect(Collectors.toList());
    }

    @Override
    public void cancelTicket(String pnr) {

        Booking b = bookingRepo.findByPnr(pnr)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (b.isCancelled())
            throw new BadRequestException("Ticket already cancelled");

        if (java.time.Duration.between(LocalDateTime.now(),
                b.getJourneyDate().atStartOfDay()).toHours() < 24)
            throw new BadRequestException("Cannot cancel within 24 hours");

        b.setCancelled(true);
        bookingRepo.save(b);
        bookingEventPublisher.publishBookingCancelled(b);

        flightClient.releaseSeats(b.getFlightId(),
                new ReserveRequest(b.getNumberOfSeats()));

    }

    private TicketResponse toResponse(Booking b, FlightSearchResponse f) {
        TicketResponse r = new TicketResponse();
        r.setPnr(b.getPnr());
        r.setCustomerName(b.getCustomerName());
        r.setEmail(b.getEmail());
        r.setJourneyDate(b.getJourneyDate());
        r.setSeatNumbers(b.getSeatNumbers());
        r.setMealPreference(b.getMealPreference());
        r.setNumberOfSeats(b.getNumberOfSeats());
        r.setCancelled(b.isCancelled());
        r.setBookingTime(b.getBookingTime());

        r.setFromPlace(f.getFromPlace());
        r.setToPlace(f.getToPlace());
        r.setDepartureTime(f.getDepartureTime());
        r.setArrivalTime(f.getArrivalTime());
        r.setAirlineName(f.getAirlineName());

        List<PassengerDto> list = b.getPassengers().stream()
                .map(px -> {
                    PassengerDto dto = new PassengerDto();
                    dto.setName(px.getName());
                    dto.setGender(px.getGender());
                    dto.setAge(px.getAge());
                    return dto;
                }).collect(Collectors.toList());
        r.setPassengers(list);

        return r;
    }
    @Override
    public List<String> getBookedSeats(String flightId) {
        return bookingRepo.findByFlightIdAndCancelledFalse(flightId)
                .stream()
                .flatMap(b -> Arrays.stream(b.getSeatNumbers().split(",")))
                .map(String::trim)
                .toList();
    }

}
