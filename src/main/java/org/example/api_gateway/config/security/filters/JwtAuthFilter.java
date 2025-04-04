package org.example.api_gateway.config.security.filters;

import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import org.example.api_gateway.config.security.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final List<Pattern> excludedPatterns = List.of(
            Pattern.compile("^/auth/login$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^/auth/sign-up$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^/auth/login$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^/auth/check-and-make-email-verification-code$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^/auth/verify-email$", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^/auth/oauth2/authorization/google.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^/auth/login/oauth2/code/google.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^/app/total-study.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^/app/get-study-by-tags-and-zones.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^/app/get-study.*", Pattern.CASE_INSENSITIVE)
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        // Attempt to retrieve the access token from the cookies
        MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
        HttpCookie tokenCookie = cookies.getFirst("accessToken"); // The cookie name you used when setting it

        // You can still optionally check the Authorization header if needed:
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (tokenCookie == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            // Fallback to header if cookie isn't present.
            String jwt = authHeader.substring(7);
            return processJwt(exchange, chain, jwt);
        }

        if (tokenCookie != null) {
            String jwt = tokenCookie.getValue();
            return processJwt(exchange, chain, jwt);
        }

        // No token found either in cookies or header; reject the request
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> processJwt(ServerWebExchange exchange, GatewayFilterChain chain, String jwt) {
        try {
            // Validate and parse the JWT (replace with your actual implementation)
            Map<String, Object> claims = jwtUtils.parseClaims(jwt);

            // For example, add claims to request headers so that downstream services can use them
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Email", claims.get("email").toString())
                    // Optionally add more headers like roles, etc.
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;  // Ensure this filter runs early in the chain
    }

    private boolean isExcludedPath(String path) {
        return excludedPatterns.stream()
                .anyMatch(pattern -> pattern.matcher(path).matches());
    }
}