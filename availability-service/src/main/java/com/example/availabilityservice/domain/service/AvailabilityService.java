package com.example.availabilityservice.domain.service;

import com.example.availabilityservice.domain.model.Availability;
import com.example.availabilityservice.domain.repo.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository repository;
    private final AvailabilityRedisService redisService;

    /**
     * The Multi-Layer Check
     * Validates if a range of dates is truly free.
     */
    public boolean isAvailable(Long listingId, List<LocalDate> requestedDates) {
        // 1. Check Database (Hard Bookings)
        List<Availability> hardBookings = repository
                    .findByListingIdAndBookedDateIn(listingId, requestedDates);
        if (!hardBookings.isEmpty()) return false;

        // 2. Check Redis Bitset & Soft Locks
        for (LocalDate date : requestedDates) {
            // Check if bit is set to 1 or if a transient lock key exists
            if (!redisService.isAvailableInBitset(listingId, date)) {
                return false;
            }
            String lockKey = "lock:listing:" + listingId + ":" + date;
            if (Boolean.TRUE.equals(redisService.hasKey(lockKey))) return false;
        }

        return true;
    }

    /**
     * Attempt to "Soft Lock" dates for checkout
     */
    @Transactional
    public boolean lockDatesForCheckout(Long listingId, List<LocalDate> dates) {
        // Double-check availability before locking
        if (!isAvailable(listingId, dates)) {
            return false;
        }

        // Apply the 10-minute Redis lock
        return redisService.acquireSoftLock(listingId, dates);
    }

    /**
     * Finalize the booking (Conversion)
     * Moves the state from Redis Lock to Database Persistence
     */
    @Transactional
    public void confirmBooking(Long listingId, List<LocalDate> dates, Long reservationId) {
        List<Availability> commitments = dates.stream().map(date -> {
            Availability availability = new Availability();
            availability.setListingId(listingId);
            availability.setBookedDate(date);
            availability.setReservationId(reservationId);
            availability.setStatus("BOOKED");

            // Sync the Bitset so it's permanently 1 (Occupied)
            redisService.updateBitset(listingId, date, true);
            return availability;
        }).collect(Collectors.toList());

        repository.saveAll(commitments);
    }
}