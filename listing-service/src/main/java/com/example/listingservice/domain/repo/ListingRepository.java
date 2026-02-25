package com.example.listingservice.domain.repo;

import com.example.listingservice.domain.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByPropertyType(String type);
}
