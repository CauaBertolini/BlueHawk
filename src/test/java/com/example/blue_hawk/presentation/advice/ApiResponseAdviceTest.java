package com.example.blue_hawk.presentation.advice;

import com.example.blue_hawk.infrastructure.security.JwtRotationFilter;
import com.example.blue_hawk.presentation.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class ApiResponseAdviceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ApiResponseAdvice advice;

    @Mock
    private ServletServerHttpRequest request;

    @Mock
    private ServletServerHttpResponse response;

    @Mock
    private HttpServletRequest servletRequest;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        advice = new ApiResponseAdvice(objectMapper);
        when(request.getServletRequest()).thenReturn(servletRequest);
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/boardgames"));
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        closeable.close();
    }

    @Test
    void shouldWrapAuthenticatedResponseWithDataAndAccessToken() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", "user-123")
        );
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, Collections.emptyList()));
        when(servletRequest.getAttribute(JwtRotationFilter.ROTATED_TOKEN_ATTRIBUTE)).thenReturn("new-access-token");

        Map<String, String> body = Map.of("name", "Catan");
        Object result = advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON, null, request, response);

        assertInstanceOf(ApiResponse.class, result);
        ApiResponse<?> apiResponse = (ApiResponse<?>) result;
        assertEquals(body, apiResponse.data());
        assertEquals("new-access-token", apiResponse.accessToken());
    }

    @Test
    void shouldWrapEmptyResponseForDeleteWithOnlyAccessToken() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", "user-123")
        );
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, Collections.emptyList()));
        when(servletRequest.getAttribute(JwtRotationFilter.ROTATED_TOKEN_ATTRIBUTE)).thenReturn("new-access-token");

        ApiResponse<Void> emptyBody = ApiResponse.empty();
        Object result = advice.beforeBodyWrite(emptyBody, null, MediaType.APPLICATION_JSON, null, request, response);

        assertInstanceOf(ApiResponse.class, result);
        ApiResponse<?> apiResponse = (ApiResponse<?>) result;
        assertNull(apiResponse.data());
        assertEquals("new-access-token", apiResponse.accessToken());
    }

    @Test
    void shouldNotWrapWhenRequestIsNotAuthenticated() {
        SecurityContextHolder.clearContext();

        Map<String, String> body = Map.of("name", "Catan");
        Object result = advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON, null, request, response);

        assertEquals(body, result);
    }
}
