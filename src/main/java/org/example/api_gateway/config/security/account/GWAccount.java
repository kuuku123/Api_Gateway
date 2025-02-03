package org.example.api_gateway.config.security.account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class GWAccount {

    @Id @GeneratedValue
    private Long id;
    private String email;
    private String password;
}
