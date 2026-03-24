package xxqqyyy.community.modules.system.vo;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import xxqqyyy.community.modules.system.dto.RoleScopeConfigItem;

/**
 * 角色视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class RoleVO {

    private Long id;

    private String roleCode;

    private String roleName;

    private Integer status;

    private Long parentRoleId;

    private Integer allowCreateChild;

    private Integer sort;

    private String remark;

    private Set<Long> permissionIds;

    private Set<RoleScopeConfigItem> scopeConfigs;

    private LocalDateTime createTime;
}

