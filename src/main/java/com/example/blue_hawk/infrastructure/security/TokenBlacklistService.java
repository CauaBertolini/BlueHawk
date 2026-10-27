package com.example.blue_hawk.infrastructure.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklist(String tokenId, Instant expiresAt) {
        if (tokenId != null) {
            blacklistedTokens.put(tokenId, expiresAt != null ? expiresAt : Instant.now().plusSeconds(86400));
            cleanupExpiredTokens();
        }
    }

    public boolean isBlacklisted(String tokenId) {
        if (tokenId == null) {
            return false;
        }
        Instant expiresAt = blacklistedTokens.get(tokenId);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            blacklistedTokens.remove(tokenId);
            return false;
        }
        return true;
    }

    private void cleanupExpiredTokens() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }

    public void clear() {
        blacklistedTokens.clear();
    }
}
