package com.fpt.sealhackathon.security;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    // Lưu access token đã logout để filter có thể chặn ngay trong runtime hiện tại.
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    // Kiểm tra token hiện tại đã bị vô hiệu hóa trong bộ nhớ hay chưa.
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
