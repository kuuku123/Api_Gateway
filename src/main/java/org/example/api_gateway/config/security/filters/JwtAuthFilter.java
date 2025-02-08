package org.example.api_gateway.config.security.filters;

import lombok.RequiredArgsConstructor;
import org.example.api_gateway.config.security.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final Set<String> excludedPaths = Set.of(
            "/auth/sign-up",
            "/auth/login"
    );
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        HttpHeaders headers = exchange.getRequest().getHeaders();
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            // Validate the JWT. You might use a library like jjwt, nimbus-jose-jwt, etc.
            // For demonstration, assume a method validateAndParseJwt() that returns a Map of claims
            try {
                // Replace this with your actual JWT parsing and validation logic
                // For example:
                // Map<String, Object> claims = jwtService.validateAndParseJwt(jwt);
                Map<String, Object> claims = jwtUtils.parseClaims(jwt);

                // Add claim values as headers, e.g., email, roles, etc.
                // This way, the downstream app server gets these details directly.
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Email", claims.get("email").toString())
                        // You can add more headers as needed:
                        // .header("X-User-Roles", claims.get("roles").toString())
                        .build();

                ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

                return chain.filter(mutatedExchange);
            } catch (Exception e) {
                // If JWT validation fails, you can either stop the chain or let it pass with an error
                e.printStackTrace();
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        // If no JWT is present or not in the expected format, you can decide to either
        // continue (if the endpoint is public) or reject the request.
        // For this example, we assume unauthorized.
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;  // Ensure this filter runs early in the chain
    }

    private boolean isExcludedPath(String path) {
        return excludedPaths.stream()
                .anyMatch(excluded -> excluded.equalsIgnoreCase(path));
    }
}