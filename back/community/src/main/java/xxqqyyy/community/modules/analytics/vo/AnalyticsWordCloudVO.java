package xxqqyyy.community.modules.analytics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Word cloud view object.
 */
@Data
@Builder
@Schema(description = "词云分析结果")
public class AnalyticsWordCloudVO {

    @Schema(description = "报告类型")
    private String reportType;

    @Schema(description = "报告日期")
    private LocalDate reportDate;

    @Schema(description = "范围类型")
    private String scopeKind;

    @Schema(description = "生成时间")
    private LocalDateTime generatedAt;

    @Schema(description = "词云词项")
    private List<WordCloudItemVO> items;
}

