package xxqqyyy.community.modules.repair.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Data;

/**
 * 提交处理结果请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class RepairSubmitRequest {

    @NotBlank(message = "完成说明不能为空")
    private String finishDesc;

    private Set<Long> attachmentFileIds;
}
