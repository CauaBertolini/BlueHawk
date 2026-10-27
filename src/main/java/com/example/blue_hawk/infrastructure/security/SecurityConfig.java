package com.example.blue_hawk.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRotationFilter jwtRotationFilter) throws Exception {
        return http
                // API stateless com Bearer token (sem cookies), então CSRF não se aplica.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/users", "/users/authenticate").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterAfter(jwtRotationFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

    @Bean
    public JwtRotationFilter jwtRotationFilter(JwtTokenService jwtTokenService) {
        return new JwtRotationFilter(jwtTokenService);
    }

    @Bean
    public FilterRegistrationBean<JwtRotationFilter> jwtRotationFilterRegistration(JwtRotationFilter filter) {
        FilterRegistrationBean<JwtRotationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(@Value("${security.jwt.secret}") String secret) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(secret)));
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret, TokenBlacklistService tokenBlacklistService) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey(secret)).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> blacklistValidator = new TokenBlacklistValidator(tokenBlacklistService);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator, blacklistValidator));
        return decoder;
    }

    public JwtDecoder jwtDecoder(String secret) {
        return jwtDecoder(secret, new TokenBlacklistService());
    }

    private static SecretKey secretKey(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
