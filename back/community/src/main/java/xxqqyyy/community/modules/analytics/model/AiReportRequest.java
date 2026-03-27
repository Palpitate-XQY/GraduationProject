package xxqqyyy.community.modules.analytics.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Structured payload sent to AI/fallback generator.
 */
@Data
@Builder
public class AiReportRequest {

    private String reportType;

    private String scopeKind;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    private Map<String, Object> metrics;

    private List<WordFrequency> topWords;

    @Data
    @Builder
    public static class WordFrequency {
        private String word;
        private long weight;
    }
}

