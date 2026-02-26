package com.example.gatewayservice.routes;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class Fallback {

    @GetMapping("/fallback")
    public ResponseEntity<?> fallback() {
        return ResponseEntity.internalServerError()
                .body(Map.of("status", "Circuit breaker open"));
    }

    @GetMapping
    public String index() {
        return "Gateway";
    }
}
