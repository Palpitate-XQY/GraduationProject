package xxqqyyy.community.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 字典类型视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "字典类型视图对象")
public class DictTypeVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
