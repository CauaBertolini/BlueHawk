package com.example.blue_hawk.infrastructure.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class TokenBlacklistValidator implements OAuth2TokenValidator<Jwt> {

    private final TokenBlacklistService tokenBlacklistService;
    private static final OAuth2Error INVALID_TOKEN_ERROR = new OAuth2Error(
            "invalid_token",
            "This token has already been used and invalidated",
            null
    );

    public TokenBlacklistValidator(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (tokenBlacklistService == null) {
            return OAuth2TokenValidatorResult.success();
        }

        boolean isBlacklisted = (jwt.getId() != null && tokenBlacklistService.isBlacklisted(jwt.getId()))
                || (jwt.getTokenValue() != null && tokenBlacklistService.isBlacklisted(jwt.getTokenValue()));

        if (isBlacklisted) {
            return OAuth2TokenValidatorResult.failure(INVALID_TOKEN_ERROR);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
