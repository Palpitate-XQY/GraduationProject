package xxqqyyy.community.modules.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 公告可见范围配置项。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "公告可见范围项")
public class NoticeScopeItem {

    @NotBlank(message = "范围类型不能为空")
    @Schema(description = "范围类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String scopeType;

    @NotNull(message = "范围引用ID不能为空")
    @Schema(description = "范围引用组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long scopeRefId;
}

