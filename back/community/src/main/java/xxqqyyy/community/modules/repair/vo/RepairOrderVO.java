package xxqqyyy.community.modules.repair.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 报修工单视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class RepairOrderVO {

    private Long id;

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

    private LocalDateTime createTime;

    private List<RepairAttachmentVO> attachments;
}
