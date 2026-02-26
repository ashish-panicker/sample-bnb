package com.example.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class GatewayServiceApplication {

    public static void main(String[] args) {
        // TraceID created at the gateway will be passed down to threads further down the service line
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(GatewayServiceApplication.class, args);
    }


}
