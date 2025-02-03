package org.example.api_gateway.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final PasswordEncoder passwordEncoder;
    private final ReactiveUserDetailsService userDetailsService;
    private final TokenProvider tokenProvider;

    @PostMapping("/login")
    Mono<ApiResponse<String>> login(@RequestBody LoginForm loginForm) {
        return userDetailsService.findByUsername(loginForm.getNicknameOrEmail())
                .filter(userDetails -> passwordEncoder.matches(loginForm.getPassword(), userDetails.getPassword()))
                .map(tokenProvider::generateToken)
                .map(token -> new ApiResponse<>("success token", HttpStatus.OK, token))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
    }
}
