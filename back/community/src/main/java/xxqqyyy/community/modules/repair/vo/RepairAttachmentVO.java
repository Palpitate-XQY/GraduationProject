package xxqqyyy.community.modules.repair.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 报修附件视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class RepairAttachmentVO {

    private Long id;

    private Long fileId;

    private String attachmentType;

    private LocalDateTime createTime;
}
