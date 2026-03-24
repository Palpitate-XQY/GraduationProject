package xxqqyyy.community.modules.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * 当前登录用户信息。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "当前登录用户信息")
public class CurrentUserVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "组织ID")
    private Long orgId;

    @Schema(description = "角色编码集合")
    private Set<String> roleCodes;

    @Schema(description = "权限编码集合")
    private Set<String> permissionCodes;
}

