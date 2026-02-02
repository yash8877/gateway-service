package com.gateway.filter;


import com.gateway.exception.InvalidJWTException;
import com.gateway.exception.JWTExpireException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory <JwtAuthenticationFilter.Config>{
    @Value("${jwt.secret}")
    private String secretKey;

    public JwtAuthenticationFilter() {
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


            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new InvalidJWTException("Missing or invalid Authorization header");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new InvalidJWTException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            try {
                if (isTokenValid(token)) {
                    // Extracting username and roles from token
                    String username = extractUsername(token);
                    String roles = extractRoles(token);

                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-Auth-User", username)
                            .header("X-Auth-Roles", roles)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                } else {
                    throw new InvalidJWTException("The token is invalid.");
                }
            } catch (ExpiredJwtException e) {
                throw new JWTExpireException("The token has expired.");
            }
        };
    }


    private boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    private String extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", String.class);
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static class Config {
    }
}
