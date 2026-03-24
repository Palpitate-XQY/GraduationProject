package xxqqyyy.community.security.service;

import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 安全主体加载服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface SecurityPrincipalService {

    /**
     * 根据用户ID加载登录主体。
     *
     * @param userId 用户ID
     * @return 登录主体，找不到返回 null
     */
    LoginPrincipal loadByUserId(Long userId);
}

