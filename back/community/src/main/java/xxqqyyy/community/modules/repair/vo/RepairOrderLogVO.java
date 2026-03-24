package xxqqyyy.community.modules.repair.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 报修流转日志视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class RepairOrderLogVO {

    private Long id;

    private Long repairOrderId;

    private String fromStatus;

    private String toStatus;

    private String operationType;

    private Long operatorUserId;

    private String operationRemark;

    private LocalDateTime operationTime;
}
