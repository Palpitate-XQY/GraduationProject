package xxqqyyy.community.modules.activity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 活动分页查询参数。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class ActivityPageQuery {

    @Min(value = 1, message = "当前页最小为1")
    private long current = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private long size = 10;

    private String keyword;

    private String status;
}

