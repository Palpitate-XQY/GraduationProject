package xxqqyyy.community.modules.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 权限实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPermission extends BaseAuditEntity {

    private String permissionCode;

    private String permissionName;

    private String permissionType;

    private Long parentId;

    private String path;

    private String method;

    private Integer sort;
}

