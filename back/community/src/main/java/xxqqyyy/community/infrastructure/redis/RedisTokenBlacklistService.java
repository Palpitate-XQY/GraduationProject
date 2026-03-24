package xxqqyyy.community.infrastructure.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xxqqyyy.community.security.service.TokenBlacklistService;

/**
 * 基于 Redis 的 token 黑名单实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String TOKEN_BLACKLIST_KEY_PREFIX = "community:auth:blacklist:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void blacklist(String tokenId, long expireSeconds) {
        if (expireSeconds <= 0) {
            return;
        }
        stringRedisTemplate.opsForValue().set(
            TOKEN_BLACKLIST_KEY_PREFIX + tokenId,
            "1",
            Duration.ofSeconds(expireSeconds)
        );
    }

    @Override
    public boolean isBlacklisted(String tokenId) {
        Boolean exists = stringRedisTemplate.hasKey(TOKEN_BLACKLIST_KEY_PREFIX + tokenId);
        return Boolean.TRUE.equals(exists);
    }
}

