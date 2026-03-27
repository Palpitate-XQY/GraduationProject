package xxqqyyy.community.modules.analytics.job;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xxqqyyy.community.modules.analytics.config.AnalyticsProperties;
import xxqqyyy.community.modules.analytics.service.AnalyticsService;

/**
 * Analytics report scheduler.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsReportScheduler {

    private final AnalyticsService analyticsService;
    private final AnalyticsProperties analyticsProperties;

    @Scheduled(
        cron = "${community.analytics.report.daily-cron:0 30 8 * * ?}",
        zone = "${community.analytics.report.zone:Asia/Shanghai}"
    )
    public void generateDailyReports() {
        if (!analyticsProperties.isEnabled()) {
            return;
        }
        LocalDate anchorDate = LocalDate.now(resolveZoneId());
        log.info("Start daily analytics report generation. anchorDate={}", anchorDate);
        analyticsService.generateTemplateReports("DAILY", anchorDate);
    }

    @Scheduled(
        cron = "${community.analytics.report.weekly-cron:0 0 9 ? * MON}",
        zone = "${community.analytics.report.zone:Asia/Shanghai}"
    )
    public void generateWeeklyReports() {
        if (!analyticsProperties.isEnabled()) {
            return;
        }
        LocalDate anchorDate = LocalDate.now(resolveZoneId()).minusWeeks(1);
        log.info("Start weekly analytics report generation. anchorDate={}", anchorDate);
        analyticsService.generateTemplateReports("WEEKLY", anchorDate);
    }

    private ZoneId resolveZoneId() {
        try {
            return ZoneId.of(analyticsProperties.getReport().getZone());
        } catch (Exception ex) {
            return ZoneId.of("Asia/Shanghai");
        }
    }
}
