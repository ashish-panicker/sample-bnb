package com.example.pricingservice.controller;

import com.example.pricingservice.domain.service.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {
    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping("/quote/{listingId}")
    public ResponseEntity<Map<String, Double>> getQuote(
            @PathVariable Long listingId,
            @RequestParam boolean isWeekend) {
        return ResponseEntity.ok(
                Map.of("price", pricingService.calculateFinalPrice(listingId, isWeekend))
        );
    }
}