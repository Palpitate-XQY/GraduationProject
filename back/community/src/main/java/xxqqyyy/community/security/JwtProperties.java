package xxqqyyy.community.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置属性。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "community.jwt")
public class JwtProperties {

    /**
     * JWT 密钥（建议使用 32 位以上随机字符串）。
     */
    private String secret;

    /**
     * JWT 过期时间（分钟）。
     */
    private long expireMinutes = 120;

    /**
     * JWT 签发者。
     */
    private String issuer = "community-service";
}

