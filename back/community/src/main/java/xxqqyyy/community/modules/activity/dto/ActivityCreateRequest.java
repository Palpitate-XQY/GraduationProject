package xxqqyyy.community.modules.activity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;

/**
 * 活动新增请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class ActivityCreateRequest {

    @NotBlank(message = "活动标题不能为空")
    private String title;

    @NotBlank(message = "活动内容不能为空")
    private String content;

    private Long coverFileId;

    private String attachmentJson;

    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime activityStartTime;

    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime activityEndTime;

    @NotNull(message = "报名开始时间不能为空")
    private LocalDateTime signupStartTime;

    @NotNull(message = "报名结束时间不能为空")
    private LocalDateTime signupEndTime;

    @NotBlank(message = "活动地点不能为空")
    private String location;

    @NotNull(message = "人数上限不能为空")
    @Positive(message = "人数上限必须为正数")
    private Integer maxParticipants;

    @Valid
    @NotEmpty(message = "活动可见范围不能为空")
    private Set<ActivityScopeItem> scopeItems;
}
