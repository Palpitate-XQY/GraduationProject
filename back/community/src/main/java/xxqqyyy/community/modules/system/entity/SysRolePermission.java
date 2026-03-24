package xxqqyyy.community.modules.system.entity;

import lombok.Data;

/**
 * 角色-权限关联实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class SysRolePermission {

    private Long id;

    private Long roleId;

    private Long permissionId;
}

