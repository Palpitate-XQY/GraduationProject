package xxqqyyy.community.modules.activity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 活动统计信息。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class ActivityStatsVO {

    private Long activityId;

    private long signupCount;

    private int maxParticipants;

    private long remainingSlots;
}

