package org.example.tnal_youth_backend.service.impl;



import org.example.tnal_youth_backend.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.tnal_youth_backend.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Override
    public String generateToken(User user) {

        return Jwts.builder()

                .subject(user.getEmail() != null
                        ? user.getEmail()
                        : user.getPhone())

                .claim("role", user.getRole().name())

                .issuedAt(new Date())

                .expiration(new Date(System.currentTimeMillis() + expiration))

                .signWith(getSigningKey(), SignatureAlgorithm.HS256)

                .compact();

    }

    @Override
    public String extractUsername(String token) {

        return extractClaims(token).getSubject();

    }

    @Override
    public boolean isTokenValid(String token, User user) {

        String username = extractUsername(token);

        String userLogin = user.getEmail() != null
                ? user.getEmail()
                : user.getPhone();

        return username.equals(userLogin)
                && !isTokenExpired(token);

    }

    private boolean isTokenExpired(String token) {

        return extractClaims(token)
                .getExpiration()
                .before(new Date());

    }

    private Claims extractClaims(String token) {

        return Jwts.parser()

                .verifyWith((SecretKey) getSigningKey())

                .build()

                .parseSignedClaims(token)

                .getPayload();

    }

    private Key getSigningKey() {

        byte[] key = Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(key);

    }

}