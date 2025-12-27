package com.flightapp.flight.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FlightSearchResponse {
    private String flightId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String airlineName;
    private double priceOneWay;
    private Double priceRoundTrip;
    private String tripType;
    private int availableSeats;
    private int totalSeats;
    private String fromPlace;
    private String toPlace;
}
