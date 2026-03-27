package xxqqyyy.community.modules.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Analytics report page query.
 */
@Data
@Schema(description = "分析报告分页查询参数")
public class AnalyticsReportPageQuery {

    @Min(value = 1, message = "当前页最小为1")
    @Schema(description = "当前页")
    private long current = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    @Schema(description = "每页条数")
    private long size = 10;

    @Schema(description = "报告类型：DAILY/WEEKLY")
    private String periodType = "DAILY";
}

