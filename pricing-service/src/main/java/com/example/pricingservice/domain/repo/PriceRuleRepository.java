package com.example.pricingservice.domain.repo;

import com.example.pricingservice.domain.model.PriceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {
    Optional<PriceRule> findByListingId(Long listingId);
}
