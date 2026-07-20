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

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration}")
    private long accessExpirationMs;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessExpirationMs);

        return Jwts.builder()
                .subject(user.getPhone())
                .issuer(issuer)
                .audience().add(audience).and()
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiration)
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
        String username = extractUsername(token);

        return username.equals(user.getPhone())
                && !isTokenExpired(token)
                && "ACCESS".equals(extractClaim(token, claims -> claims.get("type", String.class)));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
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
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}