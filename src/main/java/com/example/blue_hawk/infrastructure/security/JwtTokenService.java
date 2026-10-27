package com.example.blue_hawk.infrastructure.security;

import com.example.blue_hawk.domain.entity.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final long expirationSeconds;

    @Autowired
    public JwtTokenService(JwtEncoder jwtEncoder,
                           TokenBlacklistService tokenBlacklistService,
                           @Value("${security.jwt.expiration-seconds}") long expirationSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
        this.expirationSeconds = expirationSeconds;
    }

    public JwtTokenService(JwtEncoder jwtEncoder,
                           long expirationSeconds) {
        this(jwtEncoder, new TokenBlacklistService(), expirationSeconds);
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                // O Spring converte "scope" em authorities SCOPE_<valor> (ex.: SCOPE_ADMIN).
                .claim("scope", user.getRole().name())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    public String rotateToken(Jwt currentJwt) {
        String identifier = currentJwt.getId() != null ? currentJwt.getId() : currentJwt.getTokenValue();
        Instant expiresAt = currentJwt.getExpiresAt() != null ? currentJwt.getExpiresAt() : Instant.now().plusSeconds(expirationSeconds);
        tokenBlacklistService.blacklist(identifier, expiresAt);
        if (currentJwt.getId() != null && currentJwt.getTokenValue() != null) {
            tokenBlacklistService.blacklist(currentJwt.getTokenValue(), expiresAt);
        }

        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .id(jti)
                .subject(currentJwt.getSubject())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds));

        Map<String, Object> claims = currentJwt.getClaims();
        claims.forEach((key, value) -> {
            if (!key.equals("iss") && !key.equals("sub") && !key.equals("exp")
                    && !key.equals("iat") && !key.equals("nbf") && !key.equals("jti")) {
                builder.claim(key, value);
            }
        });

        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), builder.build()))
                .getTokenValue();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public TokenBlacklistService getTokenBlacklistService() {
        return tokenBlacklistService;
    }
}
