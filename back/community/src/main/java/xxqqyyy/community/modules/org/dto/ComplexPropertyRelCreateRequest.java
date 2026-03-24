package xxqqyyy.community.modules.org.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 小区物业关联新增请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class ComplexPropertyRelCreateRequest {

    @NotNull(message = "小区组织ID不能为空")
    private Long complexOrgId;

    @NotNull(message = "物业公司组织ID不能为空")
    private Long propertyCompanyOrgId;
}

