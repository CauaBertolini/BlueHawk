package com.example.blue_hawk.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigurationTest {

    private final CorsConfigurationSource source = new SecurityConfig()
            .corsConfigurationSource(List.of("http://localhost:5173", "https://bluehawk.example"));

    private CorsConfiguration configFor(String path) {
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest("GET", path));
        assertNotNull(config);
        return config;
    }

    @Test
    void shouldAllowConfiguredOrigins() {
        CorsConfiguration config = configFor("/boardgames");

        assertEquals("http://localhost:5173", config.checkOrigin("http://localhost:5173"));
        assertEquals("https://bluehawk.example", config.checkOrigin("https://bluehawk.example"));
    }

    @Test
    void shouldRejectUnknownOrigin() {
        assertNull(configFor("/boardgames").checkOrigin("https://evil.example"));
    }

    @Test
    void shouldExposeRotatedTokenHeaders() {
        List<String> exposed = configFor("/users/me").getExposedHeaders();

        assertNotNull(exposed);
        assertTrue(exposed.contains(JwtRotationFilter.ACCESS_TOKEN_HEADER));
        assertTrue(exposed.contains(JwtRotationFilter.NEW_TOKEN_HEADER));
    }
}
