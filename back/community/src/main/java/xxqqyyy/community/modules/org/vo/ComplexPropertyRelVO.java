package xxqqyyy.community.modules.org.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 小区物业服务关系视图。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class ComplexPropertyRelVO {

    private Long id;

    private Long complexOrgId;

    private Long propertyCompanyOrgId;

    private Integer status;
}

