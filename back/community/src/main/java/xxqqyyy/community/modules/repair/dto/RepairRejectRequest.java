package xxqqyyy.community.modules.repair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 报修驳回请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class RepairRejectRequest {

    @NotBlank(message = "驳回原因不能为空")
    private String reason;
}
