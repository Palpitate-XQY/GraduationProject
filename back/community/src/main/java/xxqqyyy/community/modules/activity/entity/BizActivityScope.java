package xxqqyyy.community.modules.activity.entity;

import lombok.Data;

/**
 * 活动可见范围实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class BizActivityScope {

    private Long id;

    private Long activityId;

    private String scopeType;

    private Long scopeRefId;
}

