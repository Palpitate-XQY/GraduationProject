package xxqqyyy.community.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 看板指标卡片视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "看板指标卡片")
public class DashboardCardVO {

    @Schema(description = "卡片编码")
    private String code;

    @Schema(description = "卡片标题")
    private String title;

    @Schema(description = "卡片数值")
    private Long value;

    @Schema(description = "卡片说明")
    private String description;
}
