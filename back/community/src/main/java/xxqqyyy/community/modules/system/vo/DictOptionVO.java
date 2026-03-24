package xxqqyyy.community.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 字典选项视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "字典选项视图对象")
public class DictOptionVO {

    @Schema(description = "字典标签")
    private String label;

    @Schema(description = "字典值")
    private String value;

    @Schema(description = "排序值")
    private Integer sort;
}
