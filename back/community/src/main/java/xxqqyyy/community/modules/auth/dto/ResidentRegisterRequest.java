package xxqqyyy.community.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 居民注册请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class ResidentRegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 32, message = "用户名长度需在4-32位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需在8-64位")
    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    private String email;

    @NotNull(message = "所属小区不能为空")
    private Long complexOrgId;

    @NotBlank(message = "房号不能为空")
    private String roomNo;

    private String emergencyContact;

    private String emergencyPhone;
}
