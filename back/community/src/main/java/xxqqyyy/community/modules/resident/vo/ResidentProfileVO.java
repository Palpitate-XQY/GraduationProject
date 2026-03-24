package xxqqyyy.community.modules.resident.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 居民档案视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "居民档案视图对象")
public class ResidentProfileVO {

    @Schema(description = "档案ID")
    private Long id;

    @Schema(description = "居民用户ID")
    private Long userId;

    @Schema(description = "社区组织ID")
    private Long communityOrgId;

    @Schema(description = "社区名称")
    private String communityOrgName;

    @Schema(description = "小区组织ID")
    private Long complexOrgId;

    @Schema(description = "小区名称")
    private String complexOrgName;

    @Schema(description = "房号")
    private String roomNo;

    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @Schema(description = "紧急联系电话")
    private String emergencyPhone;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
