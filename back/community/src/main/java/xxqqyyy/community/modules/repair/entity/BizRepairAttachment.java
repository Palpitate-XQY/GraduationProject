package xxqqyyy.community.modules.repair.entity;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 报修附件实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class BizRepairAttachment {

    private Long id;

    private Long repairOrderId;

    private Long fileId;

    private String attachmentType;

    private Long createBy;

    private LocalDateTime createTime;

    private Integer deleted;
}
