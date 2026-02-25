package com.example.listingservice.mesaging.service;

import com.example.listingservice.domain.model.Listing;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ListingProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "listing-events";

    public ListingProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendListingEvent(Listing listing, String status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("listingId", listing.getId());
        payload.put("basePrice", listing.getBasePrice());
        payload.put("status", status);
        payload.put("propertyType", listing.getPropertyType());

        kafkaTemplate.send(TOPIC, String.valueOf(listing.getId()), payload);
    }
}