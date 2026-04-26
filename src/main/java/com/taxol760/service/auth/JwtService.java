package com.taxol760.service.auth;

import com.taxol760.database.model.user.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMilliseconds;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMilliseconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMilliseconds = expirationMilliseconds;
    }

    public String generateToken(UserModel user) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expirationMilliseconds);

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    public Long extractUserId(String token) {
        String subject = extractSubject(token);

        if (subject == null) {
            return null;
        }

        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getSubject() != null && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private String extractSubject(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
