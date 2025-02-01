package org.example.gateway.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * [JWT 관련 메서드를 제공하는 클래스]
 */
@Slf4j
@Component
public class JwtUtils {

    private final Key key;
    private final long accessTokenExpTime;

    public JwtUtils(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration_time}") long accessTokenExpTime
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpTime = accessTokenExpTime;
    }

    /**
     * Access Token 생성
     * @param jwtClaimDto
     * @return Access Token String
     */
    public String createAccessToken(JwtClaimDto jwtClaimDto) {
        return createToken(jwtClaimDto, accessTokenExpTime);
    }


    /**
     * JWT 생성
     * @param jwtClaimDto
     * @param expireTime
     * @return JWT String
     */
    private String createToken(JwtClaimDto jwtClaimDto, long expireTime) {

        Claims claims = Jwts.claims()
                .add("nickname", jwtClaimDto.getNickname())
                .add("bio", jwtClaimDto.getBio())
                .add("url", jwtClaimDto.getUrl())
                .add("occupation", jwtClaimDto.getOccupation())
                .add("location", jwtClaimDto.getLocation())
                .add("email", jwtClaimDto.getEmail())
                .add("emailVerified", jwtClaimDto.isEmailVerified())
                .add("profileImage", jwtClaimDto.getProfileImage())
                .build();

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plusSeconds(expireTime);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(tokenValidity.toInstant()))
                .signWith(key)
                .compact();
    }

    /**
     * JWT 검증
     * @param token
     * @return IsValidate
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }


    /**
     * JWT Claims 추출
     * @param token
     * @return JWT Claims
     */
    public Claims parseClaims(String token) {
        Claims payload = null;
        try {
            JwtParser jwtParser = Jwts.parser()
                    .verifyWith((PublicKey) key).build();
            payload = jwtParser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
        return payload;
    }


}