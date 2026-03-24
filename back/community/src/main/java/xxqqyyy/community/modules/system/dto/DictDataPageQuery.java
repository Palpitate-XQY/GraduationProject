package xxqqyyy.community.modules.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 字典数据分页查询参数。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "字典数据分页查询参数")
public class DictDataPageQuery {

    @Min(value = 1, message = "当前页最小为1")
    @Schema(description = "当前页")
    private long current = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 200, message = "每页条数最大为200")
    @Schema(description = "每页条数")
    private long size = 10;

    @Schema(description = "字典类型编码")
    private String dictTypeCode;

    @Schema(description = "关键字（标签/值）")
    private String keyword;

    @Schema(description = "状态")
    private Integer status;
}
