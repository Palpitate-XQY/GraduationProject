package xxqqyyy.community.modules.resident.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理端维护居民档案请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "管理端维护居民档案请求")
public class ResidentProfileAdminUpdateRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "居民用户ID")
    private Long userId;

    @NotNull(message = "所属小区ID不能为空")
    @Schema(description = "所属小区组织ID")
    private Long complexOrgId;

    @Size(max = 64, message = "房号长度不能超过64")
    @Schema(description = "房号")
    private String roomNo;

    @Size(max = 64, message = "紧急联系人长度不能超过64")
    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @Pattern(regexp = "^$|^[0-9+\\-]{6,20}$", message = "紧急联系电话格式不正确")
    @Schema(description = "紧急联系电话")
    private String emergencyPhone;
}
