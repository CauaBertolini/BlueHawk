package com.example.blue_hawk.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBlacklistServiceTest {

    private TokenBlacklistService blacklistService;

    @BeforeEach
    void setUp() {
        blacklistService = new TokenBlacklistService();
    }

    @Test
    void shouldBlacklistTokenSuccessfully() {
        String tokenId = "token-123";
        assertFalse(blacklistService.isBlacklisted(tokenId));

        blacklistService.blacklist(tokenId, Instant.now().plusSeconds(60));
        assertTrue(blacklistService.isBlacklisted(tokenId));
    }

    @Test
    void shouldNotConsiderExpiredTokenAsBlacklisted() {
        String tokenId = "token-expired";
        blacklistService.blacklist(tokenId, Instant.now().minusSeconds(5));

        assertFalse(blacklistService.isBlacklisted(tokenId));
    }

    @Test
    void shouldReturnFalseForNullOrNonExistentToken() {
        assertFalse(blacklistService.isBlacklisted(null));
        assertFalse(blacklistService.isBlacklisted("non-existent"));
    }
}
