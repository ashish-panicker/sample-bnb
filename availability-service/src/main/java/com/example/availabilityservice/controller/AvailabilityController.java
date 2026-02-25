package com.example.availabilityservice.controller;

import com.example.availabilityservice.domain.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    // Check if a range of dates is available
    @GetMapping("/check/{listingId}")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Long listingId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> dates) {
        return ResponseEntity.ok(availabilityService.isAvailable(listingId, dates));
    }

    // Place a soft lock (e.g., when user clicks 'Reserve')
    @PostMapping("/lock/{listingId}")
    public ResponseEntity<String> lockDates(
            @PathVariable Long listingId,
            @RequestBody List<LocalDate> dates) {

        boolean locked = availabilityService.lockDatesForCheckout(listingId, dates);

        return locked ?
                ResponseEntity.ok("Dates locked for 10 minutes.") :
                ResponseEntity.status(409)
                        .body("One or more dates are already reserved or locked.");
    }

    @PostMapping("/confirm/{listingId}")
    public ResponseEntity<String> confirmBooking(
            @PathVariable Long listingId,
            @RequestParam Long reservationId,
            @RequestBody List<LocalDate> dates) {

        try {
            availabilityService.confirmBooking(listingId, dates, reservationId);
            return ResponseEntity.ok("Booking confirmed and persisted.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error confirming booking: " + e.getMessage());
        }
    }
}