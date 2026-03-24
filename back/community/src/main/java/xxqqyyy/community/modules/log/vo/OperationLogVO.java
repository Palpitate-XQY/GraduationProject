package xxqqyyy.community.modules.log.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 操作日志视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class OperationLogVO {

    private Long id;

    private Long userId;

    private String username;

    private String operationModule;

    private String operationType;

    private String requestUri;

    private String requestMethod;

    private Integer successFlag;

    private String errorMessage;

    private String traceId;

    private LocalDateTime operationTime;
}
