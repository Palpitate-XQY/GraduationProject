package xxqqyyy.community.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 权限新增请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class PermissionCreateRequest {

    @NotBlank(message = "权限编码不能为空")
    private String permissionCode;

    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    @NotBlank(message = "权限类型不能为空")
    private String permissionType;

    private Long parentId;

    private String path;

    private String method;

    private Integer sort;
}

