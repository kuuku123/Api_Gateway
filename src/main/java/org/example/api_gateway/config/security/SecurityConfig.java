package org.example.api_gateway.config.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.csrf.WebSessionServerCsrfTokenRepository;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         ReactiveAuthenticationManager authenticationManager,
                                                         ServerAuthenticationConverter authenticationConverter
                                                         ) {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter);

        http
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(new WebSessionServerCsrfTokenRepository()) // CSRF token stored in WebSession
                );
        http.addFilterAfter(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }


/*
    @Bean
    public ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        DefaultReactiveOAuth2UserService oAuth2UserService = new DefaultReactiveOAuth2UserService();

        return userRequest -> {
            Mono<OAuth2User> oAuth2User = oAuth2UserService.loadUser(userRequest);
            String registrationId = userRequest.getClientRegistration().getClientId();

            return oAuth2User
                    .map(OAuth2AuthenticatedPrincipal::getAttributes);
        };
    }
*/
}
