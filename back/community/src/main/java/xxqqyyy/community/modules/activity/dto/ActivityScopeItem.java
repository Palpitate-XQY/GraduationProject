package xxqqyyy.community.modules.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 活动可见范围配置项。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class ActivityScopeItem {

    @NotBlank(message = "范围类型不能为空")
    private String scopeType;

    @NotNull(message = "范围引用ID不能为空")
    private Long scopeRefId;
}

