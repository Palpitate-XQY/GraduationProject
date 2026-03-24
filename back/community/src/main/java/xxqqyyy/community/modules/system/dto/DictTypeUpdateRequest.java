package xxqqyyy.community.modules.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新字典类型请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "更新字典类型请求")
public class DictTypeUpdateRequest {

    @NotNull(message = "ID不能为空")
    @Schema(description = "字典类型ID")
    private Long id;

    @NotBlank(message = "字典编码不能为空")
    @Size(max = 64, message = "字典编码长度不能超过64")
    @Pattern(regexp = "^[a-zA-Z0-9_:-]+$", message = "字典编码仅允许字母、数字、下划线、冒号和中划线")
    @Schema(description = "字典编码")
    private String dictCode;

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 128, message = "字典名称长度不能超过128")
    @Schema(description = "字典名称")
    private String dictName;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
