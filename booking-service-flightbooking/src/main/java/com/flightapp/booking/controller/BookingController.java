package com.flightapp.booking.controller;

import com.flightapp.booking.dto.*;
import com.flightapp.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping("/{flightId}")
    public ResponseEntity<TicketResponse> book(@PathVariable String flightId,
                                               @Valid @RequestBody BookingRequest req) {
        return ResponseEntity.status(201).body(service.bookTicket(flightId, req));
    }

    @GetMapping("/ticket/{pnr}")
    public ResponseEntity<TicketResponse> ticket(@PathVariable String pnr) {
        return ResponseEntity.ok(service.getTicketByPnr(pnr));
    }

    @GetMapping("/history/{email}")
    public ResponseEntity<List<TicketResponse>> history(@PathVariable String email) {
        return ResponseEntity.ok(service.getHistory(email));
    }

    @DeleteMapping("/cancel/{pnr}")
    public ResponseEntity<String> cancel(@PathVariable String pnr) {
        service.cancelTicket(pnr);
        return ResponseEntity.ok("Ticket cancelled successfully");
    }
}
