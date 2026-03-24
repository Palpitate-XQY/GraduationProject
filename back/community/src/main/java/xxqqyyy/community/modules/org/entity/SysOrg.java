package xxqqyyy.community.modules.org.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 组织实体，承载街道/社区/小区/物业公司/部门。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysOrg extends BaseAuditEntity {

    private Long parentId;

    private String orgCode;

    private String orgName;

    private String orgType;

    private Integer status;

    private Integer sort;

    /**
     * 祖先链路径，格式示例：/1/3/。
     */
    private String ancestorPath;
}

