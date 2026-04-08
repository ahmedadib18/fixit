package com.fixit.fixit.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // =============================================
    // GET SIGNING KEY
    // =============================================
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // =============================================
    // GENERATE TOKEN
    // =============================================
    public String generateToken(String email,
                                Long userId,
                                String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                        System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(),
                        SignatureAlgorithm.HS256)
                .compact();
    }

    // =============================================
    // EXTRACT EMAIL FROM TOKEN
    // =============================================
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // =============================================
    // EXTRACT USER ID FROM TOKEN
    // =============================================
    public Long extractUserId(String token) {
        return extractAllClaims(token)
                .get("userId", Long.class);
    }

    // =============================================
    // EXTRACT USER TYPE FROM TOKEN
    // =============================================
    public String extractUserType(String token) {
        return extractAllClaims(token)
                .get("userType", String.class);
    }

    // =============================================
    // VALIDATE TOKEN
    // =============================================
    public Boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }

    // =============================================
    // CHECK IF TOKEN IS EXPIRED
    // =============================================
    private Boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // =============================================
    // EXTRACT ALL CLAIMS
    // =============================================
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}