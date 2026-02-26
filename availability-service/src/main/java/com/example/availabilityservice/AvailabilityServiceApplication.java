package com.example.availabilityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class AvailabilityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AvailabilityServiceApplication.class, args);
    }

}
