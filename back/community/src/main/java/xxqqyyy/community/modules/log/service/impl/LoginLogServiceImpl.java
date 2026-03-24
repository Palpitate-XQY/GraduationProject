package xxqqyyy.community.modules.log.service.impl;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.log.dto.LoginLogPageQuery;
import xxqqyyy.community.modules.log.entity.LogLogin;
import xxqqyyy.community.modules.log.mapper.LogLoginMapper;
import xxqqyyy.community.modules.log.service.LoginLogService;
import xxqqyyy.community.modules.log.vo.LoginLogVO;

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

    @Override
    public PageResult<LoginLogVO> page(LoginLogPageQuery query) {
        long total = logLoginMapper.countPage(query);
        if (total == 0) {
            return PageResult.empty(query.getCurrent(), query.getSize());
        }
        long offset = (query.getCurrent() - 1) * query.getSize();
        var records = logLoginMapper.selectPage(query, offset, query.getSize()).stream().map(log -> LoginLogVO.builder()
            .id(log.getId())
            .userId(log.getUserId())
            .username(log.getUsername())
            .successFlag(log.getSuccessFlag())
            .message(log.getMessage())
            .ip(log.getIp())
            .userAgent(log.getUserAgent())
            .loginTime(log.getLoginTime())
            .build()).toList();
        return PageResult.<LoginLogVO>builder()
            .records(records)
            .total(total)
            .current(query.getCurrent())
            .size(query.getSize())
            .build();
    }
}
