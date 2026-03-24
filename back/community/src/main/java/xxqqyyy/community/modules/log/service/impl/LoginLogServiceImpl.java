package xxqqyyy.community.modules.log.service.impl;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xxqqyyy.community.modules.log.entity.LogLogin;
import xxqqyyy.community.modules.log.mapper.LogLoginMapper;
import xxqqyyy.community.modules.log.service.LoginLogService;

/**
 * 登录日志服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LogLoginMapper logLoginMapper;

    @Override
    public void record(Long userId, String username, boolean success, String message, String ip, String userAgent) {
        LogLogin log = new LogLogin();
        log.setUserId(userId);
        log.setUsername(username);
        log.setSuccessFlag(success ? 1 : 0);
        log.setMessage(message);
        log.setIp(ip);
        log.setUserAgent(userAgent);
        log.setLoginTime(LocalDateTime.now());
        logLoginMapper.insert(log);
    }
}

