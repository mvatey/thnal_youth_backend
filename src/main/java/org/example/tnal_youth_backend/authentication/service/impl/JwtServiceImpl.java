package org.example.tnal_youth_backend.authentication.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private final String jwtSecret;
    private final long accessExpirationMs;
    private final String issuer;
    private final String audience;

    public JwtServiceImpl(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access-expiration}") long accessExpirationMs,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.audience}") String audience
    ) {
        this.jwtSecret = jwtSecret;
        this.accessExpirationMs = accessExpirationMs;
        this.issuer = issuer;
        this.audience = audience;
    }

    @Override
    public String generateToken(User user) {
        String username = resolveUsername(user);

        if (user.getRole() == null
                || user.getRole().name() == null
                || user.getRole().name().isBlank()) {
            throw new IllegalStateException(
                    "User does not have a valid role"
            );
        }

        Date issuedAt = new Date();
        Date expiresAt = new Date(
                issuedAt.getTime() + accessExpirationMs
        );

        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .audience()
                .add(audience)
                .and()
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("type", "ACCESS")
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isTokenValid(String token, User user) {
        try {
            String username = extractUsername(token);

            String tokenType = extractClaim(
                    token,
                    claims -> claims.get("type", String.class)
            );

            return username != null
                    && username.equals(resolveUsername(user))
                    && "ACCESS".equals(tokenType)
                    && !isTokenExpired(token);

        } catch (Exception exception) {
            return false;
        }
    }

    private String resolveUsername(User user) {
        if (user.getEmail() != null
                && !user.getEmail().isBlank()) {
            return user.getEmail().trim();
        }

        if (user.getPhone() != null
                && !user.getPhone().isBlank()) {
            return user.getPhone().trim();
        }

        throw new IllegalStateException(
                "User has no phone number or email"
        );
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(
                token,
                Claims::getExpiration
        );

        return expiration.before(new Date());
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return resolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(
                StandardCharsets.UTF_8
        );

        return Keys.hmacShaKeyFor(keyBytes);
    }
}