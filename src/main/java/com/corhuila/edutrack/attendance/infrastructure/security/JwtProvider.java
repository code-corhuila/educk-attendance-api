package com.corhuila.edutrack.attendance.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtProvider {

    public static final String EXPECTED_ISSUER = "educk-identity-api";
    public static final String EXPECTED_AUDIENCE = "edutrack";
    private static final long CLOCK_SKEW_SECONDS = 30L;
    private static final int MIN_RSA_BITS = 2048;

    private final RSAPublicKey publicKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtProvider(@Value("${jwt.public-key:}") String publicKeyPem) {
        this.publicKey = parsePublicKey(publicKeyPem);
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) {
            throw new IllegalArgumentException("Malformed JWT token");
        }
        try {
            JsonNode header = objectMapper.readTree(Base64.getUrlDecoder().decode(parts[0]));
            if (!"RS256".equals(header.path("alg").asText())) {
                throw new IllegalArgumentException("Algorithm not allowed");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unreadable JWT header", e);
        }

        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(EXPECTED_ISSUER)
                .requireAudience(EXPECTED_AUDIENCE)
                .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (claims.getExpiration() == null || claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new IllegalArgumentException("exp and sub claims are required");
        }
        UUID.fromString(claims.getSubject());
        return claims;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        String subject = claims.getSubject();
        String role = claims.get("role", String.class);
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role claim is required");
        }
        return new UsernamePasswordAuthenticationToken(
                subject,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    private static RSAPublicKey parsePublicKey(String pem) {
        if (pem == null || pem.isBlank()) {
            throw new IllegalArgumentException("JWT_PUBLIC_KEY is required");
        }
        String normalized = pem.replace("\\r", "").replace("\\n", "\n").trim();
        if (!normalized.contains("BEGIN PUBLIC KEY")) {
            throw new IllegalArgumentException("JWT_PUBLIC_KEY must be an X.509 PEM public key");
        }
        String base64 = normalized.replaceAll("-----[A-Z0-9 ]+-----", "").replaceAll("\\s+", "");
        try {
            byte[] der = Base64.getDecoder().decode(base64);
            RSAPublicKey rsaKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(der));
            if (rsaKey.getModulus().bitLength() < MIN_RSA_BITS) {
                throw new IllegalArgumentException("RSA public key must be at least 2048 bits");
            }
            return rsaKey;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("JWT_PUBLIC_KEY cannot be parsed", e);
        }
    }
}
