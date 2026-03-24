package xxqqyyy.community.modules.resident.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 居民更新本人档案请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "居民更新本人档案请求")
public class ResidentProfileMyUpdateRequest {

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
