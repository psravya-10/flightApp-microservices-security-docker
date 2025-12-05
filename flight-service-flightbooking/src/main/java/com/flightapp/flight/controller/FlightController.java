
package com.flightapp.flight.controller;

import com.flightapp.flight.dto.*;
import com.flightapp.flight.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/flight")
public class FlightController {

    private final FlightService service;

    public FlightController(FlightService service) {
        this.service = service;
    }

    @PostMapping("/airline/inventory/add")
    public ResponseEntity<String> addInventory(@Valid @RequestBody AirlineInventoryRequest req) {
        return ResponseEntity.status(201).body(service.addInventory(req));
    }

    @PostMapping("/search")
    public ResponseEntity<List<FlightSearchResponse>> search(@Valid @RequestBody FlightSearchRequest req) {
        return ResponseEntity.ok(service.searchFlights(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightSearchResponse> getFlight(@PathVariable String id) {
        return ResponseEntity.ok(service.getFlightById(id));
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<Void> reserve(@PathVariable String id, @RequestBody ReserveRequest req) {
        service.reserveSeats(id, req.getSeats());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<Void> release(@PathVariable String id, @RequestBody ReserveRequest req) {
        service.releaseSeats(id, req.getSeats());
        return ResponseEntity.ok().build();
    }
}

