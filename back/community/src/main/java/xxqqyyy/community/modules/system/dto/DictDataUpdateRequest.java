package xxqqyyy.community.modules.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新字典数据请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "更新字典数据请求")
public class DictDataUpdateRequest {

    @NotNull(message = "ID不能为空")
    @Schema(description = "字典数据ID")
    private Long id;

    @NotBlank(message = "字典类型编码不能为空")
    @Size(max = 64, message = "字典类型编码长度不能超过64")
    @Schema(description = "字典类型编码")
    private String dictTypeCode;

    @NotBlank(message = "字典标签不能为空")
    @Size(max = 128, message = "字典标签长度不能超过128")
    @Schema(description = "字典标签")
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    @Size(max = 128, message = "字典值长度不能超过128")
    @Schema(description = "字典值")
    private String dictValue;

    @NotNull(message = "排序不能为空")
    @Schema(description = "排序值")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
