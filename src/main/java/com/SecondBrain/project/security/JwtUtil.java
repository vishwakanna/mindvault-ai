package com.SecondBrain.project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component  // Registers this as a Spring-managed bean (singleton, injectable anywhere)
public class JwtUtil {

    @Value("${jwt.secret}")          // Reads from application.properties
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Lazily build the signing key from the secret string
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ── Generate a token for a user ─────────────────────────────────────────
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);  // Custom data embedded inside the token

        return Jwts.builder()
                .claims(claims)                              // Custom claims (role)
                .subject(email)                              // "sub" field — who this token is for
                .issuedAt(new Date())                        // "iat" — when issued
                .expiration(new Date(System.currentTimeMillis() + expiration))  // "exp" — expiry
                .signWith(getSigningKey())                   // Sign with HMAC-SHA256
                .compact();                                  // Build final string
    }

    // ── Extract the email (subject) from a token ────────────────────────────
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ── Extract the role from a token ───────────────────────────────────────
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ── Check if token is valid for a given email ───────────────────────────
    public boolean isTokenValid(String token, String email) {
        try {
            String extractedEmail = extractEmail(token);
            return extractedEmail.equals(email) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;  // Any parsing/signature error = invalid
        }
    }

    // ── Internal: parse and return all claims ───────────────────────────────
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // Verify signature using our secret
                .build()
                .parseSignedClaims(token)
                .getPayload();                // Returns the decoded claims object
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}