package com.flightapp.booking.feign;

import com.flightapp.booking.dto.FlightSearchResponse;
import com.flightapp.booking.dto.ReserveRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "flight-service-flightbooking")
public interface FlightClient {

    @GetMapping("/api/flight/{id}")
    FlightSearchResponse getFlightById(@PathVariable String id);

    @PostMapping("/api/flight/{id}/reserve")
    void reserveSeats(@PathVariable String id, @RequestBody ReserveRequest req);

    @PostMapping("/api/flight/{id}/release")
    void releaseSeats(@PathVariable String id, @RequestBody ReserveRequest req);

}

