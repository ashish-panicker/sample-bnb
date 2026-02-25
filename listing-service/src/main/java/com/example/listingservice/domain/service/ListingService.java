package com.example.listingservice.domain.service;

import com.example.listingservice.domain.model.Listing;
import com.example.listingservice.domain.repo.ListingRepository;
import com.example.listingservice.mesaging.service.ListingProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingProducer producer;
    private final ListingRepository repository;

    @Transactional
    public Listing save(Listing listing) {
        var savedListing = repository.save(listing);
        producer.sendListingEvent(savedListing, "CREATED");
        return listing;
    }

    public Optional<Listing> findById(Long id) {
        return repository.findById(id);
    }

    public List<Listing> findAll() {
        return repository.findAll();
    }
}
