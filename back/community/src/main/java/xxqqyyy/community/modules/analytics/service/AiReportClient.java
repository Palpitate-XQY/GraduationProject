package xxqqyyy.community.modules.analytics.service;

import xxqqyyy.community.modules.analytics.model.AiReportRequest;
import xxqqyyy.community.modules.analytics.model.AiReportResult;

/**
 * Report generation client abstraction.
 */
public interface AiReportClient {

    /**
     * Whether this generator is available.
     */
    boolean isEnabled();

    /**
     * Generate report summary markdown.
     */
    AiReportResult generate(AiReportRequest request);
}
