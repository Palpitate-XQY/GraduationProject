package xxqqyyy.community.modules.analytics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Analytics report view object.
 */
@Data
@Builder
@Schema(description = "分析报告")
public class AnalyticsReportVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "报告类型")
    private String reportType;

    @Schema(description = "报告日期")
    private LocalDate reportDate;

    @Schema(description = "范围类型")
    private String scopeKind;

    @Schema(description = "范围键")
    private String scopeKey;

    @Schema(description = "统计开始时间")
    private LocalDateTime periodStart;

    @Schema(description = "统计结束时间")
    private LocalDateTime periodEnd;

    @Schema(description = "指标")
    private Map<String, Object> metrics;

    @Schema(description = "报告正文 Markdown")
    private String summaryMarkdown;

    @Schema(description = "报告正文 HTML")
    private String summaryHtml;

    @Schema(description = "AI 模式")
    private String aiMode;

    @Schema(description = "模型名")
    private String modelName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "生成时间")
    private LocalDateTime generatedAt;
}

