package com.example.listingservice.runner;

import com.example.listingservice.domain.model.Listing;
import com.example.listingservice.domain.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListingRunner implements CommandLineRunner {

    private final ListingService repository;

    @Override
    public void run(String... args) throws Exception {

        // 1. Cozy Beachfront Cottage
        Listing cottage = new Listing();
        cottage.setTitle("Cozy Beachfront Cottage");
        cottage.setDescription("A beautiful 1-bedroom cottage right on the sand.");
        cottage.setAddress("123 Ocean Drive, Malibu");
        cottage.setPropertyType("Cottage");
        cottage.setMaxGuests(2);
        cottage.setBasePrice(150.0);
        cottage.setHostId(101L);
        repository.save(cottage);

        // 2. Modern Loft in Soho
        Listing loft = new Listing();
        loft.setTitle("Modern Loft in Soho");
        loft.setDescription("Industrial style loft with high ceilings and city views.");
        loft.setAddress("45 Spring St, New York");
        loft.setPropertyType("Apartment");
        loft.setMaxGuests(4);
        loft.setBasePrice(350.0);
        loft.setHostId(102L);
        repository.save(loft);

        // 3. Rustic Mountain Cabin
        Listing cabin = new Listing();
        cabin.setTitle("Rustic Mountain Cabin");
        cabin.setDescription("Escape to the woods in this hand-built cedar cabin.");
        cabin.setAddress("789 Pine Way, Aspen");
        cabin.setPropertyType("Cabin");
        cabin.setMaxGuests(6);
        cabin.setBasePrice(200.0);
        cabin.setHostId(103L);
        repository.save(cabin);

        // 4. Luxury Villa with Pool
        Listing villa = new Listing();
        villa.setTitle("Luxury Villa with Pool");
        villa.setDescription("Infinity pool overlooking the Mediterranean sea.");
        villa.setAddress("Villa 5, Santorini");
        villa.setPropertyType("Villa");
        villa.setMaxGuests(8);
        villa.setBasePrice(1200.0);
        villa.setHostId(104L);
        repository.save(villa);


    }
}
