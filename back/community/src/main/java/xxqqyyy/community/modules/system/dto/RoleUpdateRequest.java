package xxqqyyy.community.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

/**
 * 角色更新请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class RoleUpdateRequest {

    @NotNull(message = "角色ID不能为空")
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "是否允许创建下级角色不能为空")
    private Integer allowCreateChild;

    private Integer sort;

    private String remark;

    @NotNull(message = "权限集合不能为空")
    private Set<Long> permissionIds;

    @NotNull(message = "数据范围配置不能为空")
    private Set<RoleScopeConfigItem> scopeConfigs;
}

