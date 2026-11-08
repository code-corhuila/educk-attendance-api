package com.corhuila.edutrack.attendance.infrastructure.security;

import com.corhuila.edutrack.attendance.domain.port.in.GetAttendanceUseCase;
import com.corhuila.edutrack.attendance.domain.port.in.RegisterAttendanceUseCase;
import com.corhuila.edutrack.attendance.infrastructure.web.AttendanceController;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class Rs256SecurityTest {

    private static KeyPair kp;
    private static JwtProvider provider;
    private static MockMvc mockMvc;

    @BeforeAll
    static void setUp() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        kp = kpg.generateKeyPair();
        String pem = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(kp.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----";
        provider = new JwtProvider(pem);

        RegisterAttendanceUseCase regUseCase = mock(RegisterAttendanceUseCase.class);
        GetAttendanceUseCase getUseCase = mock(GetAttendanceUseCase.class);
        when(getUseCase.getAllAttendance()).thenReturn(List.of());
        mockMvc = MockMvcBuilders.standaloneSetup(new AttendanceController(regUseCase, getUseCase))
                .addFilters(new JwtAuthenticationFilter(provider, new ObjectMapper()))
                .build();
    }

    @Test
    void acceptsValidRs256AndRejectsTamperedOrUnsupportedTokens() throws Exception {
        String valid = Jwts.builder().issuer("educk-identity-api").audience().add("edutrack").and()
                .subject(UUID.randomUUID().toString()).claim("role", "TEACHER")
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(kp.getPrivate(), Jwts.SIG.RS256).compact();
        assertTrue(provider.validateToken(valid));
        mockMvc.perform(get("/api/v1/attendance").header("Authorization", "Bearer " + valid))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/attendance"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));

        String hs = Jwts.builder().issuer("educk-identity-api").audience().add("edutrack").and()
                .subject(UUID.randomUUID().toString()).claim("role", "TEACHER")
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8))).compact();
        assertFalse(provider.validateToken(hs));
        mockMvc.perform(get("/api/v1/attendance").header("Authorization", "Bearer " + hs)).andExpect(status().isUnauthorized());

        String hNone = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String body = Base64.getUrlEncoder().withoutPadding().encodeToString(("{\"iss\":\"educk-identity-api\",\"aud\":\"edutrack\",\"sub\":\"" + UUID.randomUUID() + "\",\"role\":\"TEACHER\",\"exp\":" + Instant.now().plusSeconds(300).getEpochSecond() + "}").getBytes(StandardCharsets.UTF_8));
        assertFalse(provider.validateToken(hNone + "." + body + ".sig"));

        String expired = Jwts.builder().issuer("educk-identity-api").audience().add("edutrack").and()
                .subject(UUID.randomUUID().toString()).claim("role", "TEACHER")
                .expiration(Date.from(Instant.now().minusSeconds(120)))
                .signWith(kp.getPrivate(), Jwts.SIG.RS256).compact();
        assertFalse(provider.validateToken(expired));

        String wrongAud = Jwts.builder().issuer("educk-identity-api").audience().add("wrong").and()
                .subject(UUID.randomUUID().toString()).claim("role", "TEACHER")
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(kp.getPrivate(), Jwts.SIG.RS256).compact();
        assertFalse(provider.validateToken(wrongAud));

        mockMvc.perform(get("/api/v1/attendance/health")).andExpect(status().isOk());
    }
}
