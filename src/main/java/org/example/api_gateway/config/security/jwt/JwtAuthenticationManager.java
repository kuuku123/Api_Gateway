package org.example.api_gateway.config.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
                .cast(JwtToken.class)
                .flatMap(jwtToken ->
                        // Wrap the token validation in a fromCallable for asynchronous execution
                        Mono.fromCallable(() -> jwtService.isTokenValid(jwtToken.getToken()))
                                // Use a scheduler for potentially blocking calls
                                .subscribeOn(Schedulers.boundedElastic())
                                // Handle the boolean result
                                .flatMap(valid -> {
                                    if (valid) {
                                        return Mono.just(jwtToken.withAuthenticated(true));
                                    } else {
                                        return Mono.error(new JwtAuthenticationException("Invalid token"));
                                    }
                                })
                                // Convert any JwtAuthenticationException thrown during validation
                                .onErrorMap(JwtAuthenticationException.class, e -> e)
                );
    }

}
