package xxqqyyy.community.modules.repair.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报修分派请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class RepairAssignRequest {

    @NotNull(message = "维修员不能为空")
    private Long maintainerUserId;

    private String remark;
}
