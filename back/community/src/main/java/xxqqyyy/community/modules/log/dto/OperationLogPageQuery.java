package xxqqyyy.community.modules.log.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 操作日志分页查询参数。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class OperationLogPageQuery {

    @Min(value = 1, message = "当前页最小为1")
    private long current = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private long size = 10;

    private String username;

    private String operationModule;

    private String operationType;

    private Integer successFlag;

    private String traceId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
