package xxqqyyy.community.modules.org.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 组织新增请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class OrgCreateRequest {

    private Long parentId;

    @NotBlank(message = "组织编码不能为空")
    private String orgCode;

    @NotBlank(message = "组织名称不能为空")
    private String orgName;

    @NotBlank(message = "组织类型不能为空")
    private String orgType;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "排序不能为空")
    private Integer sort;
}

