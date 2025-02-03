package org.example.api_gateway.config.security.account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GWAccountRepository extends JpaRepository<GWAccount, Long> {

  GWAccount findByEmail(String email);
}
