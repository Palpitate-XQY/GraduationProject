package xxqqyyy.community.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * SecurityContext 读取辅助工具。
 *
 * @author codex
 * @since 1.0.0
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {
    }

    /**
     * 获取当前登录主体，未登录时抛出业务异常。
     *
     * @return 登录主体
     */
    public static LoginPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginPrincipal principal)) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }
}

