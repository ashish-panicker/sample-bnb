package com.example.listingservice.controller;

import com.example.listingservice.domain.model.Listing;
import com.example.listingservice.domain.repo.ListingRepository;
import com.example.listingservice.domain.service.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService service;


    @PostMapping
    public ResponseEntity<Listing> createListing(@RequestBody Listing listing) {
        return ResponseEntity.ok(service.save(listing));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listing> getListing(@PathVariable Long id,
                                              @RequestHeader("Authorization") String token) {
        log.debug("Token: {}", token);
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Listing> getAllListings() {
        return service.findAll();
    }
}