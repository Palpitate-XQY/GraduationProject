package xxqqyyy.community.modules.log.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 登录日志实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class LogLogin {

    private Long id;

    private Long userId;

    private String username;

    private Integer successFlag;

    private String message;

    private String ip;

    private String userAgent;

    private LocalDateTime loginTime;
}

