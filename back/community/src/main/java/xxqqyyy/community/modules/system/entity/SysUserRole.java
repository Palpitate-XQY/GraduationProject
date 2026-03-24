package xxqqyyy.community.modules.system.entity;

import lombok.Data;

/**
 * 用户-角色关联实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class SysUserRole {

    private Long id;

    private Long userId;

    private Long roleId;
}

