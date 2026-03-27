package xxqqyyy.community.modules.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.analytics.dto.AnalyticsGenerateRequest;
import xxqqyyy.community.modules.analytics.dto.AnalyticsReportPageQuery;
import xxqqyyy.community.modules.analytics.service.AnalyticsService;
import xxqqyyy.community.modules.analytics.vo.AnalyticsReportVO;
import xxqqyyy.community.modules.analytics.vo.AnalyticsWordCloudVO;
import xxqqyyy.community.modules.log.annotation.OperationLog;

/**
 * Analytics controller.
 */
@Tag(name = "智能分析中心")
@RestController
@RequestMapping("/api/system/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "词云分析")
    @GetMapping("/wordcloud")
    @PreAuthorize("hasAuthority('analytics:wordcloud:view')")
    public ApiResponse<AnalyticsWordCloudVO> wordCloud(
        @RequestParam(value = "periodType", defaultValue = "DAILY") String periodType,
        @RequestParam(value = "anchorDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate anchorDate
    ) {
        return ApiResponse.success(analyticsService.getWordCloud(periodType, anchorDate));
    }

    @Operation(summary = "最新报告")
    @GetMapping("/reports/latest")
    @PreAuthorize("hasAuthority('analytics:report:view')")
    public ApiResponse<AnalyticsReportVO> latest(
        @RequestParam(value = "periodType", defaultValue = "DAILY") String periodType
    ) {
        return ApiResponse.success(analyticsService.getLatestReport(periodType));
    }

    @Operation(summary = "报告分页")
    @GetMapping("/reports/page")
    @PreAuthorize("hasAuthority('analytics:report:view')")
    public ApiResponse<PageResult<AnalyticsReportVO>> page(@Valid AnalyticsReportPageQuery query) {
        return ApiResponse.success(analyticsService.pageReports(query));
    }

    @Operation(summary = "手动生成报告")
    @PostMapping("/reports/generate")
    @PreAuthorize("hasAuthority('analytics:report:generate')")
    @OperationLog(module = "智能分析", type = "手动生成")
    public ApiResponse<AnalyticsReportVO> generate(@Valid @RequestBody AnalyticsGenerateRequest request) {
        return ApiResponse.success(analyticsService.generateReport(request));
    }
}
