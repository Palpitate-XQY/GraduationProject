package xxqqyyy.community.modules.system.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

/**
 * 用户角色分配请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class UserRoleAssignRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色ID集合不能为空")
    private Set<Long> roleIds;
}

