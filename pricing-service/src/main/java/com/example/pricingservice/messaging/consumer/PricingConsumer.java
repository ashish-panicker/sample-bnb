package com.example.pricingservice.messaging.consumer;

import com.example.pricingservice.domain.model.PriceRule;
import com.example.pricingservice.domain.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PricingConsumer {

    private final PricingService service;

    @KafkaListener(topics = "listing-events", groupId = "pricing-group")
    public void handleListingEvent(Map<String, Object> message) {
        Long listingId = Long.valueOf(message.get("listingId").toString());
        double basePrice = Double.parseDouble(message.get("basePrice").toString());
        String status = (String) message.get("status");
        String propertyType = (String) message.get("propertyType");

        if (!"CREATED".equals(status)) {
            log.info("Not running price rule for {}", listingId);
            return;
        }
        log.info("Creating Price Rule: for {}", listingId);
        PriceRule rule = switch (propertyType) {
            case "villa" -> createRule(listingId, basePrice, 1.5, 250.0, 0.15);
            case "cabin" -> createRule(listingId, basePrice, 1.3, 100.0, 0.1);
            case "apartment" -> createRule(listingId, basePrice, 1.1, 75.0, 0.08);
            default -> createRule(listingId, basePrice, 1.2, 50.0, 0.1);
        };
        service.save(rule);
    }

    private PriceRule createRule(Long id, Double base, double mult, double clean, double service) {
        PriceRule rule = new PriceRule();
        rule.setListingId(id);
        rule.setBasePrice(base);
        rule.setWeekendMultiplier(mult);
        rule.setCleaningFee(clean);
        rule.setServiceFeeRate(service);
        return rule;
    }
}