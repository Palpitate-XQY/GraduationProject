package xxqqyyy.community.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 字典数据视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "字典数据视图对象")
public class DictDataVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "字典类型编码")
    private String dictTypeCode;

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "字典值")
    private String dictValue;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
