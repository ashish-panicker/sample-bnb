package com.example.availabilityservice.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityRedisService {

    private final StringRedisTemplate redisTemplate;

    // Constants for Redis Keys
    private static final String BITSET_PREFIX = "calendar:bitset:";
    private static final String LOCK_PREFIX = "lock:listing:";

    /**
     * The Day Index
     * By using ChronoUnit.DAYS.between(LocalDate.now(), date), we turn a date like 2026-12-25 into a simple integer (e.g., 300).
     * This allows us to store an entire year of availability in just 365 bits (approx. 46 bytes).
     * This is incredibly memory-efficient.
     *
     * The Soft Lock:
     * When acquireSoftLock is called, it uses the Redis SETNX command.
     * This is atomic. If two requests hit Redis at the exact same millisecond, only one will receive true.
     *
     * Automatic Cleanup:
     * Because the Soft Lock has a Duration.ofMinutes(10), we don't need to write code to "unlock"
     * dates if a user abandons their cart. Redis deletes the lock automatically.
     */

    /**
     * Layer 1: The Bitset (Long-term view)
     * Sets a date as Available (0) or Booked (1).
     */
    public void updateBitset(Long listingId, LocalDate date, boolean isBooked) {
        String key = BITSET_PREFIX + listingId;
        long dayIndex = ChronoUnit.DAYS.between(LocalDate.now(), date);

        // We set the bit to 'true' if it is booked/unavailable
        redisTemplate.opsForValue().setBit(key, dayIndex, isBooked);
    }

    /**
     * Layer 2: The Soft Lock (Temporary checkout lock)
     * Returns true if the lock was successfully acquired.
     */
    public boolean acquireSoftLock(Long listingId, List<LocalDate> dates) {
        for (LocalDate date : dates) {
            String lockKey = LOCK_PREFIX + listingId + ":" + date;

            // SET IF ABSENT (NX) with a 10-minute expiration
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "LOCKED", Duration.ofMinutes(10));

            if (Boolean.FALSE.equals(success)) {
                return false; // Someone else is currently checking out these dates
            }
        }
        return true;
    }

    /**
     * Check if a date is free in the Bitset
     */
    public boolean isAvailableInBitset(Long listingId, LocalDate date) {
        String key = BITSET_PREFIX + listingId;
        long dayIndex = ChronoUnit.DAYS.between(LocalDate.now(), date);
        Boolean isOccupied = redisTemplate.opsForValue().getBit(key, dayIndex);
        return isOccupied != null && !isOccupied;
    }

    public void releaseSoftLock(Long listingId, LocalDate date) {
        String lockKey = "lock:listing:" + listingId + ":" + date.toString();
        redisTemplate.delete(lockKey);
    }

    public void delete(Long listingId) {
        String key = BITSET_PREFIX + listingId;
        redisTemplate.delete(key);
    }

    public Boolean hasKey(String lockKey) {
        return redisTemplate.hasKey(lockKey);
    }
}
