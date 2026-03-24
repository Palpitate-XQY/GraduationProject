package xxqqyyy.community.modules.system.vo;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 权限视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class PermissionVO {

    private Long id;

    private String permissionCode;

    private String permissionName;

    private String permissionType;

    private Long parentId;

    private String path;

    private String method;

    private Integer sort;

    private LocalDateTime createTime;
}

