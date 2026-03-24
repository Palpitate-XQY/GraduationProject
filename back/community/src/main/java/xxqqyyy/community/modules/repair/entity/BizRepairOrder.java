package xxqqyyy.community.modules.repair.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 报修工单实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizRepairOrder extends BaseAuditEntity {

    private String orderNo;

    private String title;

    private String description;

    private String contactPhone;

    private String repairAddress;

    private String emergencyLevel;

    private LocalDateTime expectHandleTime;

    private String status;

    private Long communityOrgId;

    private Long complexOrgId;

    private Long propertyCompanyOrgId;

    private Long residentUserId;

    private Long acceptUserId;

    private Long assignUserId;

    private Long maintainerUserId;

    private String processDesc;

    private String finishDesc;

    private String rejectReason;

    private String residentFeedback;

    private Integer evaluateScore;

    private String evaluateContent;

    private Integer urgeCount;

    private LocalDateTime acceptedTime;

    private LocalDateTime assignedTime;

    private LocalDateTime processingTime;

    private LocalDateTime finishTime;

    private LocalDateTime confirmTime;

    private LocalDateTime closedTime;

    private LocalDateTime lastUrgeTime;
}
