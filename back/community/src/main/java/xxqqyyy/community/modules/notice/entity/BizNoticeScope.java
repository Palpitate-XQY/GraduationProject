package xxqqyyy.community.modules.notice.entity;

import lombok.Data;

/**
 * 公告可见范围实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class BizNoticeScope {

    private Long id;

    private Long noticeId;

    private String scopeType;

    private Long scopeRefId;
}

