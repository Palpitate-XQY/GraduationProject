package xxqqyyy.community.modules.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 用户分页查询参数。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "用户分页查询参数")
public class UserPageQuery {

    @Min(value = 1, message = "当前页最小为1")
    @Schema(description = "当前页")
    private long current = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    @Schema(description = "每页条数")
    private long size = 10;

    @Schema(description = "关键字（用户名/昵称）")
    private String keyword;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "组织ID")
    private Long orgId;
}

