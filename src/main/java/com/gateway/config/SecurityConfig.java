package com.gateway.config;

import  com.gateway.filter.JwtAuthenticationFilter;
import com.gateway.filter.RoleBasedAuthorizationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    @Value("${USER_SERVICE_URL}")
    private String userServiceUrl;

    @Value("${BOOKING_SERVICE_URL}")
    private String bookingServiceUrl;

    @Value("${EVENT_SERVICE_URL}")
    private String eventServiceUrl;

    @Value("${PAYMENT_SERVICE_URL}")
    private String paymentServiceUrl;

    @Value("${NOTIFICATION_SERVICE_URL}")
    private String notificationServiceUrl;

    private final JwtAuthenticationFilter authFilter;

    private final RoleBasedAuthorizationFilter roleFilter;

    public SecurityConfig(JwtAuthenticationFilter authFilter, RoleBasedAuthorizationFilter roleFilter) {
        this.authFilter = authFilter;
        this.roleFilter = roleFilter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth service routes
                .route("auth-service", r -> r
                        .path("/auth/**" ,"/v3/api-docs/", "/swagger-ui/**", "/swagger-ui.html")
                        .uri(authServiceUrl))

                .route("auth-service", r-> r
                        .path("/auth/delete/id/{userId}")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(getRoleFilterForAdmin())
                        )
                        .uri(authServiceUrl)
                )

                //Event service routes
                .route("event-service",r-> r
                        .path("/events/add-event","/events/id/{id}","/events/delete/id/{id}","/events/update/{id}")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(getRoleFilterForAdmin())
                        )
                        .uri(eventServiceUrl))
                .route("event-service",r-> r
                        .path("/events/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(eventServiceUrl))

                //User Service routes
                .route("user-service",r-> r
                        .path("/users/get-all","/users/delete/{id}")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(getRoleFilterForAdmin())
                        )
                        .uri(userServiceUrl))


                .route("user-service",r-> r
                        .path("/users/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(userServiceUrl))


                //booking service routes
                .route("booking-service",r-> r
                        .path("/bookings/get-AllBookings")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(getRoleFilterForAdmin())
                        )
                        .uri(bookingServiceUrl))

                .route("booking-service",r-> r
                        .path("/bookings/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(bookingServiceUrl))


                //payment service routes
                .route("payment-service",r-> r
                        .path("/payments/**")
                        .filters(f -> f
                                .filter(authFilter.apply(new JwtAuthenticationFilter.Config()))
                        )
                        .uri(paymentServiceUrl))


                //notification service
                .route("notification-service",r-> r
                        .path("/notify/**")
                        .uri(notificationServiceUrl))

                .build();
    }

    private GatewayFilter getRoleFilterForAdmin() {
        RoleBasedAuthorizationFilter.Config config = new RoleBasedAuthorizationFilter.Config();
        config.setRequiredRoles(List.of("ROLE_ADMIN"));
        return roleFilter.apply(config);
    }

}
