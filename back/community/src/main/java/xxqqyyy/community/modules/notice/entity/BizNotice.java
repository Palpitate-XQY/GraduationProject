package xxqqyyy.community.modules.notice.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 公告主表实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizNotice extends BaseAuditEntity {

    private String noticeType;

    private String title;

    private String content;

    private Long coverFileId;

    private String attachmentJson;

    private String status;

    private Integer topFlag;

    private Long publisherOrgId;

    private LocalDateTime publishTime;
}

