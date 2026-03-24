package xxqqyyy.community.infrastructure.redis;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/**
 * 活动报名并发控制器。
 * 使用 Redis Lua 保证“校验名额 + 占位”原子执行。
 *
 * @author codex
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ActivitySignupLimiter {

    private static final long EXPIRE_SECONDS = 86400L;
    private static final String KEY_PREFIX = "community:activity:signup:count:";

    private final StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
        """
            local count = redis.call('GET', KEYS[1])
            if not count then
              count = tonumber(ARGV[1])
            else
              count = tonumber(count)
            end
            local max = tonumber(ARGV[2])
            if count >= max then
              return -1
            end
            count = count + 1
            redis.call('SET', KEYS[1], count, 'EX', ARGV[3])
            return count
            """, Long.class
    );

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
        """
            local count = redis.call('GET', KEYS[1])
            if not count then
              count = tonumber(ARGV[1])
            else
              count = tonumber(count)
            end
            if count <= 0 then
              redis.call('SET', KEYS[1], 0, 'EX', ARGV[2])
              return 0
            end
            count = count - 1
            redis.call('SET', KEYS[1], count, 'EX', ARGV[2])
            return count
            """, Long.class
    );

    /**
     * 预占一个报名名额。
     *
     * @param activityId 活动ID
     * @param currentCount 当前已报名人数（DB真实值）
     * @param maxParticipants 最大人数
     * @return 是否预占成功
     */
    public boolean reserve(Long activityId, long currentCount, int maxParticipants) {
        Long result = stringRedisTemplate.execute(
            RESERVE_SCRIPT,
            Collections.singletonList(key(activityId)),
            String.valueOf(currentCount),
            String.valueOf(maxParticipants),
            String.valueOf(EXPIRE_SECONDS)
        );
        return result != null && result >= 0;
    }

    /**
     * 释放一个报名名额。
     *
     * @param activityId 活动ID
     * @param currentCount 当前已报名人数（DB真实值）
     */
    public void release(Long activityId, long currentCount) {
        stringRedisTemplate.execute(
            RELEASE_SCRIPT,
            Collections.singletonList(key(activityId)),
            String.valueOf(currentCount),
            String.valueOf(EXPIRE_SECONDS)
        );
    }

    /**
     * 删除报名计数缓存，通常在活动状态变化后调用。
     *
     * @param activityId 活动ID
     */
    public void evict(Long activityId) {
        stringRedisTemplate.delete(key(activityId));
    }

    private String key(Long activityId) {
        return KEY_PREFIX + activityId;
    }
}
