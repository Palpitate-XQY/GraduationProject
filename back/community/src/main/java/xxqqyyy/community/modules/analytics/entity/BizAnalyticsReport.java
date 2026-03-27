package xxqqyyy.community.modules.analytics.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Analytics report snapshot entity.
 */
@Data
public class BizAnalyticsReport {

    private Long id;

    private String reportType;

    private LocalDate reportDate;

    private String scopeKey;

    private String scopeKind;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    private String metricsJson;

    private String wordcloudJson;

    private String summaryMarkdown;

    private String aiMode;

    private String modelName;

    private String status;

    private LocalDateTime generatedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

