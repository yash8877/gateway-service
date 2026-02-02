package com.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleBasedAuthorizationFilter extends AbstractGatewayFilterFactory<RoleBasedAuthorizationFilter.Config> {

    public RoleBasedAuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String path = request.getURI().getPath();
            if (path.startsWith("/auth/") || path.equals("/register") || path.equals("/login")) {
                return chain.filter(exchange);
            }

            String rolesHeader = request.getHeaders().getFirst("X-Auth-Roles");
            if (rolesHeader == null || rolesHeader.isEmpty()) {
                return onError(exchange, "No roles found", HttpStatus.FORBIDDEN);
            }

            List<String> userRoles = Arrays.asList(rolesHeader.split(","));
            boolean hasRequiredRole = false;

            for (String requiredRole : config.requiredRoles) {
                if (userRoles.contains(requiredRole)) {
                    hasRequiredRole = true;
                    break;
                }
            }

            if (!hasRequiredRole) {
                return onError(exchange, "Insufficient permissions", HttpStatus.FORBIDDEN);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {
        private List<String> requiredRoles;

        public List<String> getRequiredRoles() {
            return requiredRoles;
        }

        public void setRequiredRoles(List<String> requiredRoles) {
            this.requiredRoles = requiredRoles;
        }
    }
}