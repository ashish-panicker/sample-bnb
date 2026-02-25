package com.example.pricingservice.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PriceRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long listingId;

    private double basePrice;
    private double weekendMultiplier;
    private double cleaningFee;
    private double serviceFeeRate;
}
