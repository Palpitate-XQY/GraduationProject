package xxqqyyy.community.modules.analytics.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Time window descriptor for analytics report.
 */
@Data
@Builder
public class AnalyticsPeriodWindow {

    private String reportType;

    private LocalDate anchorDate;

    private LocalDate reportDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime previousStartTime;

    private LocalDateTime previousEndTime;
}

