package com.example.availabilityservice.messaging.consumer;

import com.example.availabilityservice.domain.service.AvailabilityRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AvailabilityConsumer {

    private final AvailabilityRedisService redisService;

    @KafkaListener(topics = "listing-events", groupId = "availability-group")
    public void consumeListingEvent(Map<String, Object> message) {
        Long listingId = Long.valueOf(message.get("listingId").toString());
        String status = (String) message.get("status");

        log.info("Received event for Listing {}: Status {}", listingId, status);

        // Using Java Switch Expression for clean state handling
        switch (status) {
            case "CREATED" -> {
                initializeCalendar(listingId);
                log.info("Calendar initialized for Listing {}", listingId);
            }
            case "DELETED" -> {
                purgeCalendar(listingId);
                log.info("Calendar purged for Listing {}", listingId);
            }
            default -> log.warn("Unhandled status: {}", status);
        }
    }

    private void initializeCalendar(Long listingId) {
        // We set today's bit to 'Available' (false/0)
        // This effectively 'touches' the Redis Bitset, creating the key
        redisService.updateBitset(listingId, LocalDate.now(), false);
    }

    private void purgeCalendar(Long listingId) {
        // Logic to wipe Redis keys and potentially DB records if a listing is removed
        redisService.delete(listingId);
    }
}