package com.gateway.config;

import com.gateway.filter.JwtAuthenticationFilter;
import com.gateway.filter.RoleBasedAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter authFilter;

    @Autowired
    private RoleBasedAuthorizationFilter roleFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth service routes
                .route("auth-service", r -> r
                        .path("/auth/**" ,"/v3/api-docs/", "/swagger-ui/**", "/swagger-ui.html")
                        .uri("lb://auth-service"))

                .route("auth-service", r-> r
                        .path("/auth/delete/id/{userId}")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(getRoleFilterForAdmin())
                        )
                        .uri("lb://auth-service")
                )

                //Event service routes
                .route("event-service",r-> r
                        .path("/events/add-event","/events/id/{id}","/events/delete/id/{id}","/events/update/{id}")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(getRoleFilterForAdmin())
                        )
                        .uri("lb://event-service"))
                .route("event-service",r-> r
                        .path("/events/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri("lb://event-service"))

                //User Service routes
                .route("user-service",r-> r
                        .path("/users/get-all","/users/delete/{id}")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(getRoleFilterForAdmin())
                        )
                        .uri("lb://user-service"))


                .route("user-service",r-> r
                        .path("/users/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri("lb://user-service"))


                //booking service routes
                .route("booking-service",r-> r
                        .path("/bookings/get-AllBookings")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(getRoleFilterForAdmin())
                        )
                        .uri("lb://booking-service"))

                .route("booking-service",r-> r
                        .path("/bookings/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri("lb://booking-service"))


                //payment service routes
                .route("payment-service",r-> r
                        .path("/payments/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri("lb://payment-service"))


                //notification service
                .route("notification-service",r-> r
                        .path("/notify/**")
                        .uri("lb://notification-service"))

                .build();
    }

    private GatewayFilter getRoleFilterForAdmin() {
        RoleBasedAuthorizationFilter.Config config = new RoleBasedAuthorizationFilter.Config();
        config.setRequiredRoles(List.of("ROLE_ADMIN"));
        return roleFilter.apply(config);
    }

}
