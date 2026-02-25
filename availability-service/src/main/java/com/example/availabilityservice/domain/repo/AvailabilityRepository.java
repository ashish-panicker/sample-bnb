package com.example.availabilityservice.domain.repo;

import com.example.availabilityservice.domain.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    // Used to check if any dates in a range are already in the DB
    List<Availability> findByListingIdAndBookedDateIn(Long listingId, List<LocalDate> dates);

    void deleteByListingId(Long listingId);
}
