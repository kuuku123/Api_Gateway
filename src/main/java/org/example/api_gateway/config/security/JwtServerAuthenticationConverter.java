package org.example.api_gateway.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    private final JwtService jwtService;
    private static final String BEARER ="Bearer ";

    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(BEARER))
                .map(header -> header.substring(BEARER.length()))
                .publishOn(Schedulers.boundedElastic())
                .handle((token, sink) -> {
                    try {
                        sink.next(new JwtToken(token, createUserDetails(token)));
                    } catch (JwtAuthenticationException e) {
                        sink.error(new RuntimeException(e));
                    }
                });
    }

    private UserDetails createUserDetails(String token) throws JwtAuthenticationException {
        String username = jwtService.extractUsername(token);
        return User.builder()
                .username(username)
                .authorities(createAuthorities(token))
                .password("")
                .build();
    }

    private List<SimpleGrantedAuthority> createAuthorities(String token) throws JwtAuthenticationException {
        return jwtService.extractRoles(token).stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
