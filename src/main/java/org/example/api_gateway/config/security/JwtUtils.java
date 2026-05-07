package org.example.api_gateway.config.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * [JWT 검증 전용 클래스 - RSA 공개 키로만 검증합니다]
 * Gateway는 공개 키만 보유하므로 토큰을 위조할 수 없습니다.
 */
@Slf4j
@Component
public class JwtUtils {

    private final PublicKey publicKey;

    public JwtUtils(@Value("${jwt.public-key}") String publicKeyPem) {
        this.publicKey = loadPublicKey(publicKeyPem);
    }

    /**
     * PEM 문자열에서 RSA PublicKey 로드 (X.509 형식)
     */
    private PublicKey loadPublicKey(String pem) {
        try {
            String stripped = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(stripped);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key", e);
        }
    }

    /**
     * JWT Claims 추출 (RS256 검증) — 검증 실패 또는 만료 시 null 반환
     *
     * @param token JWT 문자열
     * @return Claims, or null if the token is invalid/expired
     */
    public Claims parseClaims(String token) {
        try {
            JwtParser jwtParser = Jwts.parser()
                    .verifyWith(publicKey).build();
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token");
            return null;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token signature");
            return null;
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token");
            return null;
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty");
            return null;
        }
    }
}