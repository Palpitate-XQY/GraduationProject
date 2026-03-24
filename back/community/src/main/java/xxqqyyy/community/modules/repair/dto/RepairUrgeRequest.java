package xxqqyyy.community.modules.repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 报修催单请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class RepairUrgeRequest {

    @NotBlank(message = "催单内容不能为空")
    private String content;
}
