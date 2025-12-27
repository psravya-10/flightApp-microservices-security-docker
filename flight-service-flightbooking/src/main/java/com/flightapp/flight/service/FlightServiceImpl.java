package com.flightapp.flight.service;

import com.flightapp.flight.dto.*;
import com.flightapp.flight.entity.*;
import com.flightapp.flight.exception.*;
import com.flightapp.flight.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlightServiceImpl implements FlightService {

    private final AirlineRepository airlineRepo;
    private final FlightRepository flightRepo;

    public FlightServiceImpl(AirlineRepository airlineRepo, FlightRepository flightRepo) {
        this.airlineRepo = airlineRepo;
        this.flightRepo = flightRepo;
    }

    @Override
    public String addInventory(AirlineInventoryRequest req) {

        Airline airline = airlineRepo.findByNameIgnoreCase(req.getAirlineName())
                .orElseGet(() -> airlineRepo.save(new Airline(null, req.getAirlineName())));

        Optional<Flight> existing = flightRepo
                .findByAirlineIdAndFromPlaceIgnoreCaseAndToPlaceIgnoreCaseAndDepartureTime(
                        airline.getId(),
                        req.getFromPlace(),
                        req.getToPlace(),
                        req.getDepartureTime()
                );

        if (existing.isPresent())
            throw new BadRequestException("Flight already exists for this airline and schedule");

        Flight f = new Flight();
        f.setAirlineId(airline.getId());
        f.setAirlineName(airline.getName());
        f.setFromPlace(req.getFromPlace());
        f.setToPlace(req.getToPlace());
        f.setDepartureTime(req.getDepartureTime());
        f.setArrivalTime(req.getArrivalTime());
        f.setTripType(req.getTripType());
        f.setTotalSeats(req.getTotalSeats());
        f.setAvailableSeats(req.getTotalSeats());
        f.setPriceOneWay(req.getPriceOneWay());
        f.setPriceRoundTrip(req.getPriceRoundTrip());

        Flight saved = flightRepo.save(f);
        return saved.getId();
    }

    @Override
    public List<FlightSearchResponse> searchFlights(FlightSearchRequest req) {

        LocalDateTime start = req.getJourneyDate().atStartOfDay();
        LocalDateTime end = req.getJourneyDate().atTime(23, 59);

        List<Flight> flights = flightRepo
                .findByFromPlaceIgnoreCaseAndToPlaceIgnoreCaseAndDepartureTimeBetweenAndTripTypeIgnoreCase(
                        req.getFromPlace(),
                        req.getToPlace(),
                        start,
                        end,
                        req.getTripType()
                );

        return flights.stream().map(f -> {
            FlightSearchResponse r = new FlightSearchResponse();
            r.setFlightId(f.getId());
            r.setDepartureTime(f.getDepartureTime());
            r.setArrivalTime(f.getArrivalTime());
            r.setAirlineName(f.getAirlineName());
            r.setPriceOneWay(f.getPriceOneWay());
            r.setPriceRoundTrip(f.getPriceRoundTrip());
            r.setTripType(f.getTripType());
            r.setAvailableSeats(f.getAvailableSeats());
            r.setTotalSeats(f.getTotalSeats());
            r.setFromPlace(f.getFromPlace());
            r.setToPlace(f.getToPlace());
            return r;
        }).collect(Collectors.toList());
    }

    @Override
    public FlightSearchResponse getFlightById(String id) {
        Flight f = flightRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Flight not found"));

        FlightSearchResponse r = new FlightSearchResponse();
        r.setFlightId(f.getId());
        r.setDepartureTime(f.getDepartureTime());
        r.setArrivalTime(f.getArrivalTime());
        r.setAirlineName(f.getAirlineName());
        r.setPriceOneWay(f.getPriceOneWay());
        r.setPriceRoundTrip(f.getPriceRoundTrip());
        r.setTripType(f.getTripType());
        r.setAvailableSeats(f.getAvailableSeats());
        r.setTotalSeats(f.getTotalSeats());
        r.setFromPlace(f.getFromPlace());
        r.setToPlace(f.getToPlace());
        return r;
    }

    @Override
    public void reserveSeats(String id, int seats) {
        Flight f = flightRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Flight not found"));

        if (seats <= 0)
            throw new BadRequestException("Invalid seat count");

        if (f.getAvailableSeats() < seats)
            throw new BadRequestException("Seats not available");

        f.setAvailableSeats(f.getAvailableSeats() - seats);
        flightRepo.save(f);
    }

    @Override
    public void releaseSeats(String id, int seats) {
        Flight f = flightRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Flight not found"));

        if (seats <= 0)
            throw new BadRequestException("Invalid seat count");

        f.setAvailableSeats(f.getAvailableSeats() + seats);
        if (f.getAvailableSeats() > f.getTotalSeats())
            f.setAvailableSeats(f.getTotalSeats());

        flightRepo.save(f);
    }
}
