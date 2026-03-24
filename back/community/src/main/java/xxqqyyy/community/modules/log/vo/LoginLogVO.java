package xxqqyyy.community.modules.log.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 登录日志视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class LoginLogVO {

    private Long id;

    private Long userId;

    private String username;

    private Integer successFlag;

    private String message;

    private String ip;

    private String userAgent;

    private LocalDateTime loginTime;
}
