package xxqqyyy.community.modules.repair.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Data;

/**
 * 居民反馈未解决请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class RepairReopenRequest {

    @NotBlank(message = "未解决反馈不能为空")
    private String feedback;

    private Set<Long> attachmentFileIds;
}
