package xxqqyyy.community.modules.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 权限更新请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class PermissionUpdateRequest {

    @NotNull(message = "权限ID不能为空")
    private Long id;

    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    @NotBlank(message = "权限类型不能为空")
    private String permissionType;

    private Long parentId;

    private String path;

    private String method;

    private Integer sort;
}

