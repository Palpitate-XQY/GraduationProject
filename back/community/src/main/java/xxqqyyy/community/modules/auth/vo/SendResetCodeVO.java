package xxqqyyy.community.modules.auth.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 发送验证码响应。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class SendResetCodeVO {

    /**
     * 验证码有效秒数。
     */
    private long expireSeconds;

    /**
     * 一期无短信网关，返回联调验证码。
     */
    private String debugCode;
}
