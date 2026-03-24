package xxqqyyy.community.modules.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Data;

/**
 * 用户新增请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "用户新增请求")
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 32, message = "用户名长度需在4-32位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需在8-64位")
    private String password;

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

