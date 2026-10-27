package com.example.blue_hawk.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtRotationFilter extends OncePerRequestFilter {

    public static final String ROTATED_TOKEN_ATTRIBUTE = "ROTATED_ACCESS_TOKEN";
    public static final String ACCESS_TOKEN_HEADER = "accessToken";
    public static final String NEW_TOKEN_HEADER = "X-New-Token";

    private final JwtTokenService jwtTokenService;

    public JwtRotationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth && authentication.isAuthenticated()) {
            String newToken = jwtTokenService.rotateToken(jwtAuth.getToken());
            request.setAttribute(ROTATED_TOKEN_ATTRIBUTE, newToken);
            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newToken);
            response.setHeader(ACCESS_TOKEN_HEADER, newToken);
            response.setHeader(NEW_TOKEN_HEADER, newToken);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                    HttpHeaders.AUTHORIZATION + ", " + ACCESS_TOKEN_HEADER + ", " + NEW_TOKEN_HEADER);
        }

        filterChain.doFilter(request, response);
    }
}
