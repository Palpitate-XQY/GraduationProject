package xxqqyyy.community.modules.system.entity;

import lombok.Data;

/**
 * 角色数据范围关联实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class SysRoleScope {

    private Long id;

    private Long roleId;

    private String scopeType;

    private Long scopeRefId;
}

