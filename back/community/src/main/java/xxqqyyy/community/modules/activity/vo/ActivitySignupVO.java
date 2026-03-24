package xxqqyyy.community.modules.activity.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 活动报名名单视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class ActivitySignupVO {

    private Long id;

    private Long activityId;

    private Long userId;

    private String signupStatus;

    private LocalDateTime signupTime;

    private LocalDateTime cancelTime;
}

