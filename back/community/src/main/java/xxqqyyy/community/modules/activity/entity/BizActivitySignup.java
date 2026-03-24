package xxqqyyy.community.modules.activity.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 活动报名实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizActivitySignup extends BaseAuditEntity {

    private Long activityId;

    private Long userId;

    private String signupStatus;

    private LocalDateTime signupTime;

    private LocalDateTime cancelTime;
}

