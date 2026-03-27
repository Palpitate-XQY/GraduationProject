package xxqqyyy.community.modules.analytics.model;

import lombok.Builder;
import lombok.Data;

/**
 * AI generation result payload.
 */
@Data
@Builder
public class AiReportResult {

    private String markdown;

    private String aiMode;

    private String modelName;
}

