package xxqqyyy.community.modules.log.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 操作日志实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class LogOperation {

    private Long id;

    private Long userId;

    private String username;

    private String operationModule;

    private String operationType;

    private String requestUri;

    private String requestMethod;

    private String requestBody;

    private String responseBody;

    private Integer successFlag;

    private String errorMessage;

    private String traceId;

    private LocalDateTime operationTime;
}

