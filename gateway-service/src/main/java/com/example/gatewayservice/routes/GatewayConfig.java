package com.example.gatewayservice.routes;

import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4jBulkheadProvider;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions.circuitBreaker;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.rewritePath;
import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions.tokenRelay;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

// https://spring.io/projects/spring-cloud-gateway
@Configuration
public class GatewayConfig {

    @Bean
    public Customizer<Resilience4jBulkheadProvider> bulkheadProviderCustomizer(ThreadPoolBulkheadRegistry threadPoolRegistry) {
        return provider -> {
            provider.configure(builder ->
                    builder.threadPoolBulkheadConfig(
                                    threadPoolRegistry
                                            .getConfiguration("pricing_thread_pool")
                                            .orElse(ThreadPoolBulkheadConfig.ofDefaults()))
                            .build(), "pricing_cb");
            provider.configure(builder ->
                    builder.threadPoolBulkheadConfig(
                                    threadPoolRegistry
                                            .getConfiguration("listing_thread_pool")
                                            .orElse(ThreadPoolBulkheadConfig.ofDefaults()))
                            .build(), "listing_cb");
            provider.configure(builder ->
                    builder.threadPoolBulkheadConfig(
                                    threadPoolRegistry
                                            .getConfiguration("availability_thread_pool")
                                            .orElse(ThreadPoolBulkheadConfig.ofDefaults()))
                            .build(), "availability_cb");

        };
    }

    @Bean
    public Function<ServerRequest, String> userKeyResolver() {
        return request -> request.remoteAddress()
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("anonymous");
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes(Function<ServerRequest, String> userKeyResolver) {

        var services = Map.of(
                "listings", "http://localhost:9081",
                "pricing", "http://localhost:9082",
                "availability", "http://localhost:9083"
        );

//        var listingCB = circuitBreaker("listing_cb", URI.create("forward:/fallback"));
//        var pricingCB = circuitBreaker("pricing_cb", URI.create("forward:/fallback"));
//        var availabilityCB = circuitBreaker("availability_cb", URI.create("forward:/fallback"));
//
//        var rateLimiter = rateLimit(r -> r
//                .setCapacity(10) // Allow up to 10 requests simultaneously
//                .setPeriod(Duration.ofSeconds(1)) // How frequently the quota is refilled
//                .setKeyResolver(userKeyResolver)); // IP Resolver

        RouterFunctions.Builder builder = route();

        services.forEach((name, url) -> {
            builder.add(route(name + "_service")
                    .route(path("/api/v1/" + name + "/**"), http(url))
                    .filter(tokenRelay())
//                    .filter(rateLimiter)
                    .filter(circuitBreaker(name + "_cb", URI.create("forward:/fallback")))
                    .build()
            );
        });

        return builder.build();

//        return route("listing_service")
//                .filter(listingCB)
//                .filter(rateLimiter)
//                .route(
//                        path("/api/v1/listings/**"), http("http://localhost:9081")
//                )
//                .build()
//                .and(
//                        route("pricing_service")
//                                .route(path("/api/v1/pricing/**"), http("http://localhost:9082"))
//                                .filter(pricingCB)
//                                .filter(rateLimiter)
//                                .build()
//                )
//                .and(
//                        route("availability_service")
//                                .route(path("/api/v1/availability/**"), http("http://localhost:9083"))
//                                .filter(availabilityCB)
//                                .build()
//                ).and(
//                        route("availability_server_mgmt")
//                                .route(path("/api/v1/availability/mgmt/**"), http("http://localhost:9083"))
//                                // http://localhost:9080/api/v1/availability/mgmt/actuator ->
//                                // http://localhost:9083/actuator
//                                .filter(listingCB)
//                                .filter(rateLimiter)
//                                .before(rewritePath("/api/v1/availability/mgmt/(?<segment>.*)", "/actuator/{segment}"))
//                                .build()
//                );
    }

    /**
     * spring.cloud.gateway.globalcors.add-to-simple-url-handler-mapping=true
     * spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedOrigins=http://localhost:3000
     * spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedMethods[0]=GET
     * spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedMethods[1]=POST
     * spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedMethods[2]=PUT
     * spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedMethods[3]=DELETE
     * spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedMethods[4]=OPTIONS
     * spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedHeaders=*
     * spring.cloud.gateway.globalcors.cors-configurations.[/**].allowCredentials=true
     * spring.cloud.gateway.globalcors.cors-configurations.[/**].maxAge=3600
     */

    @Bean
    public CorsFilter gatewayCorsFilter() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }

}
