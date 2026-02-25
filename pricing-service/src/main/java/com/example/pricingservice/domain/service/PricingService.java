package com.example.pricingservice.domain.service;

import com.example.pricingservice.domain.model.PriceRule;
import com.example.pricingservice.domain.repo.PriceRuleRepository;
import org.springframework.stereotype.Service;

@Service
public class PricingService {
    private final PriceRuleRepository repository;

    public PricingService(PriceRuleRepository repository) {
        this.repository = repository;
    }


    public Double calculateFinalPrice(Long listingId, boolean isWeekend) {
        PriceRule rule = repository.findByListingId(listingId)
                .orElseThrow(() -> new RuntimeException("Pricing rules not found for listing: " + listingId));

        double price = rule.getBasePrice();

        if (isWeekend) {
            price *= rule.getWeekendMultiplier();
        }

        double serviceFee = price * rule.getServiceFeeRate();
        return price + serviceFee + rule.getCleaningFee();
    }

    public PriceRule save(PriceRule rule) {
        return repository.save(rule);
    }
}
