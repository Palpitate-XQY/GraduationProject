package xxqqyyy.community.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色数据范围配置项。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class RoleScopeConfigItem {

    @NotBlank(message = "范围类型不能为空")
    private String scopeType;

    private Long scopeRefId;
}

