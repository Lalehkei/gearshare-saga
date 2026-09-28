package com.gearshare.booking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Issues and validates the JWTs booking-service's own REST API uses. There is no
 * cross-service token trust anywhere in this project -- catalog-service, approval-service,
 * payment-service and handover-service have never heard of this class, and never will:
 * they only ever talk to each other over Kafka, inside the docker network, which is not
 * reachable from outside it. This is the "edge auth vs. internal trust" split the README
 * describes.
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String base64Secret,
                       @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Returns null (rather than throwing) for anything malformed, expired, or tampered
     * with -- the filter that calls this just treats a null username as "not
     * authenticated" and lets Spring Security's normal 401/403 handling take it from there.
     */
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Rejected an invalid or expired JWT: {}", e.getMessage());
            return null;
        }
    }

    public boolean isValid(String token, UserDetails userDetails) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            Date expiration = extractClaim(token, Claims::getExpiration);
            return username.equals(userDetails.getUsername()) && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
