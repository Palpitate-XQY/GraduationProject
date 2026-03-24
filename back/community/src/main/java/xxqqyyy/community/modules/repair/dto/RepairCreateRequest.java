package xxqqyyy.community.modules.repair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;

/**
 * 居民创建报修请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class RepairCreateRequest {

    @NotBlank(message = "报修标题不能为空")
    private String title;

    @NotBlank(message = "问题描述不能为空")
    private String description;

    @NotBlank(message = "联系方式不能为空")
    private String contactPhone;

    @NotBlank(message = "报修位置不能为空")
    private String repairAddress;

    @NotBlank(message = "紧急程度不能为空")
    private String emergencyLevel;

    private LocalDateTime expectHandleTime;

    @NotNull(message = "所属小区不能为空")
    private Long complexOrgId;

    private Long propertyCompanyOrgId;

    private Set<Long> attachmentFileIds;
}
