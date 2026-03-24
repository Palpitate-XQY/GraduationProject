package xxqqyyy.community.modules.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

/**
 * 用户更新请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "用户更新请求")
public class UserUpdateRequest {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private String phone;

    private String email;

    @NotNull(message = "组织不能为空")
    private Long orgId;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "角色不能为空")
    private Set<Long> roleIds;
}

