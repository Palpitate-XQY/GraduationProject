package xxqqyyy.community.modules.analytics.service;

import java.time.LocalDate;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.analytics.dto.AnalyticsGenerateRequest;
import xxqqyyy.community.modules.analytics.dto.AnalyticsReportPageQuery;
import xxqqyyy.community.modules.analytics.vo.AnalyticsReportVO;
import xxqqyyy.community.modules.analytics.vo.AnalyticsWordCloudVO;

/**
 * Analytics service.
 */
public interface AnalyticsService {

    AnalyticsWordCloudVO getWordCloud(String periodType, LocalDate anchorDate);

    AnalyticsReportVO getLatestReport(String periodType);

    PageResult<AnalyticsReportVO> pageReports(AnalyticsReportPageQuery query);

    AnalyticsReportVO generateReport(AnalyticsGenerateRequest request);

    void generateTemplateReports(String periodType, LocalDate anchorDate);
}
