package xxqqyyy.community.modules.repair.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 报修工单流转日志实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class BizRepairOrderLog {

    private Long id;

    private Long repairOrderId;

    private String fromStatus;

    private String toStatus;

    private String operationType;

    private Long operatorUserId;

    private String operationRemark;

    private LocalDateTime operationTime;
}
