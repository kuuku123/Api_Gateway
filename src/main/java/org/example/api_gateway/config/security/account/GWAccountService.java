package org.example.api_gateway.config.security.account;

import lombok.RequiredArgsConstructor;
import org.example.api_gateway.config.security.LoginForm;
import org.example.api_gateway.config.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class GWAccountService {

  private final GWAccountRepository gwAccountRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;


  public Mono<GWAccount> saveGWAccount(LoginForm loginForm) {
    return Mono.fromCallable(() -> {
      GWAccount gwAccount = new GWAccount();
      gwAccount.setEmail(loginForm.getEmail());
      gwAccount.setPassword(passwordEncoder.encode(loginForm.getPassword()));
      return gwAccountRepository.save(gwAccount);  // Blocking call
    }).subscribeOn(Schedulers.boundedElastic());  // Offload to separate thread pool
  }

  public Mono<GWAccount> getGWAccount(String email) {
    return Mono.fromCallable(() -> gwAccountRepository.findByEmail(email))
        .subscribeOn(Schedulers.boundedElastic()); // Offload blocking call
  }
}
