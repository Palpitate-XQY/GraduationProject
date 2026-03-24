package xxqqyyy.community.modules.repair.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Data;

/**
 * 报修处理请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class RepairProcessRequest {

    @NotBlank(message = "处理说明不能为空")
    private String processDesc;

    private Set<Long> attachmentFileIds;
}
