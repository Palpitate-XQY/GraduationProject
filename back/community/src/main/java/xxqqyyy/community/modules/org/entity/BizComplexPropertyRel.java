package xxqqyyy.community.modules.org.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 小区与物业公司服务关系实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizComplexPropertyRel extends BaseAuditEntity {

    private Long complexOrgId;

    private Long propertyCompanyOrgId;

    private Integer status;
}

