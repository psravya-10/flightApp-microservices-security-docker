package com.flightapp.booking.service;

import com.flightapp.booking.dto.*;
import java.util.List;

public interface BookingService {
    TicketResponse bookTicket(String flightId, BookingRequest req);
    TicketResponse getTicketByPnr(String pnr);
    List<TicketResponse> getHistory(String email);
    void cancelTicket(String pnr);
    List<String> getBookedSeats(String flightId);
   

}
