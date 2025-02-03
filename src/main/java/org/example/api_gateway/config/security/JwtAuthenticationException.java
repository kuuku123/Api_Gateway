package org.example.api_gateway.config.security;

import javax.naming.AuthenticationException;

class JwtAuthenticationException extends AuthenticationException {
    JwtAuthenticationException(String msg) {
        super(msg);
    }
}
