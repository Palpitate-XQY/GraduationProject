package xxqqyyy.community.modules.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 角色实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseAuditEntity {

    private String roleCode;

    private String roleName;

    private Integer status;

    private Long parentRoleId;

    private Integer allowCreateChild;

    private Integer sort;

    private String remark;
}

