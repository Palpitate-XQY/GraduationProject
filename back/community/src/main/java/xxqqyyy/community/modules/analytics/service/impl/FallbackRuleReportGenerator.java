package xxqqyyy.community.modules.analytics.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import xxqqyyy.community.modules.analytics.enums.AnalyticsAiModeEnum;
import xxqqyyy.community.modules.analytics.model.AiReportRequest;
import xxqqyyy.community.modules.analytics.model.AiReportResult;
import xxqqyyy.community.modules.analytics.service.AiReportClient;

/**
 * Rule-based fallback report generator.
 */
@Component
public class FallbackRuleReportGenerator implements AiReportClient {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public AiReportResult generate(AiReportRequest request) {
        Map<String, Object> metrics = request.getMetrics();
        long noticeCount = getLong(metrics, "noticePublishedCount");
        double noticeTrend = getDouble(metrics, "noticePublishedTrend");
        long activityCount = getLong(metrics, "activityPublishedCount");
        double activityTrend = getDouble(metrics, "activityPublishedTrend");
        long signupCount = getLong(metrics, "activitySignupCount");
        double signupTrend = getDouble(metrics, "activitySignupTrend");
        long repairCreated = getLong(metrics, "repairCreatedCount");
        double repairCreatedTrend = getDouble(metrics, "repairCreatedTrend");
        long repairFinished = getLong(metrics, "repairFinishedCount");
        double repairFinishedTrend = getDouble(metrics, "repairFinishedTrend");
        long repairReopened = getLong(metrics, "repairReopenedCount");
        double repairReopenedTrend = getDouble(metrics, "repairReopenedTrend");
        long urgeCount = getLong(metrics, "repairUrgeCount");
        double urgeTrend = getDouble(metrics, "repairUrgeTrend");
        double avgMinutes = getDouble(metrics, "repairAvgHandleMinutes");
        double avgMinutesTrend = getDouble(metrics, "repairAvgHandleTrend");
        double finishRate = repairCreated <= 0 ? 0D : (double) repairFinished / (double) repairCreated * 100D;

        StringBuilder markdown = new StringBuilder();
        markdown.append("# 社区运营").append("DAILY".equalsIgnoreCase(request.getReportType()) ? "日报" : "周报").append("\n\n");
        markdown.append("**统计区间：** ")
            .append(request.getPeriodStart().format(TIME_FORMATTER))
            .append(" ~ ")
            .append(request.getPeriodEnd().format(TIME_FORMATTER))
            .append("\n\n");

        markdown.append("## 一、整体概览\n");
        markdown.append("- 本期公告发布 **").append(noticeCount).append("** 条，活动发布 **").append(activityCount).append("** 场，活动报名 **")
            .append(signupCount).append("** 人次。\n");
        markdown.append("- 报修新增 **").append(repairCreated).append("** 单，完结 **").append(repairFinished).append("** 单，完结率 **")
            .append(formatDouble(finishRate)).append("%**。\n");
        markdown.append("- 平均处理时长 **").append(formatDouble(avgMinutes)).append("** 分钟，催单 **").append(urgeCount).append("** 次。\n\n");

        markdown.append("### 核心指标看板\n");
        markdown.append("| 指标 | 本期值 | 环比 |\n");
        markdown.append("|---|---:|---:|\n");
        markdown.append("| 公告发布 | ").append(noticeCount).append(" | ").append(formatTrend(noticeTrend)).append(" |\n");
        markdown.append("| 活动发布 | ").append(activityCount).append(" | ").append(formatTrend(activityTrend)).append(" |\n");
        markdown.append("| 活动报名 | ").append(signupCount).append(" | ").append(formatTrend(signupTrend)).append(" |\n");
        markdown.append("| 报修新增 | ").append(repairCreated).append(" | ").append(formatTrend(repairCreatedTrend)).append(" |\n");
        markdown.append("| 报修完结 | ").append(repairFinished).append(" | ").append(formatTrend(repairFinishedTrend)).append(" |\n");
        markdown.append("| 报修重开 | ").append(repairReopened).append(" | ").append(formatTrend(repairReopenedTrend)).append(" |\n");
        markdown.append("| 催单次数 | ").append(urgeCount).append(" | ").append(formatTrend(urgeTrend)).append(" |\n");
        markdown.append("| 平均处理时长(分钟) | ").append(formatDouble(avgMinutes)).append(" | ").append(formatTrend(avgMinutesTrend)).append(" |\n\n");

        markdown.append("## 二、热点词云解读\n");
        List<AiReportRequest.WordFrequency> topWords = new ArrayList<>(request.getTopWords() == null ? List.of() : request.getTopWords());
        topWords.sort(Comparator.comparingLong(AiReportRequest.WordFrequency::getWeight).reversed());
        if (topWords.isEmpty()) {
            markdown.append("- 本期有效语料较少，建议补充公告、活动和工单文本描述质量。\n");
        } else {
            int limit = Math.min(8, topWords.size());
            for (int i = 0; i < limit; i++) {
                AiReportRequest.WordFrequency item = topWords.get(i);
                markdown.append("- `").append(item.getWord()).append("` 词频 ").append(item.getWeight()).append("，建议纳入专项跟进。\n");
            }
        }
        markdown.append("\n");

        markdown.append("## 三、风险提醒\n");
        if (repairReopened > 0) {
            markdown.append("- 本期存在重开工单，建议复盘维修闭环与沟通记录，降低重复返修。\n");
        } else {
            markdown.append("- 本期未出现重开工单，建议保持验收标准并持续追踪满意度。\n");
        }
        if (urgeCount > 0) {
            markdown.append("- 存在催单行为，需关注响应时效和高频故障点位。\n");
        } else {
            markdown.append("- 本期无催单记录，说明响应稳定，但仍需关注峰值时段承载能力。\n");
        }
        if (repairCreated > repairFinished) {
            markdown.append("- 报修新增高于完结，建议调度更多执行角色参与处置。\n");
        } else {
            markdown.append("- 报修完结效率较稳，建议继续优化预防性巡检。\n");
        }
        markdown.append("\n");

        markdown.append("## 四、行动建议\n");
        markdown.append("- 建议按词云 Top 词建立“问题专题看板”，跟踪责任人和处置时限。\n");
        markdown.append("- 对催单与重开工单设置日常预警阈值，超阈值自动触发班组复盘。\n");
        markdown.append("- 将高频公告与活动主题同步到居民端，形成“通知-活动-服务”联动闭环。\n");

        return AiReportResult.builder()
            .markdown(markdown.toString())
            .aiMode(AnalyticsAiModeEnum.FALLBACK.getCode())
            .modelName("rule-fallback-v1")
            .build();
    }

    private long getLong(Map<String, Object> metrics, String key) {
        if (metrics == null) {
            return 0L;
        }
        Object value = metrics.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignore) {
                return 0L;
            }
        }
        return 0L;
    }

    private double getDouble(Map<String, Object> metrics, String key) {
        if (metrics == null) {
            return 0D;
        }
        Object value = metrics.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignore) {
                return 0D;
            }
        }
        return 0D;
    }

    private String formatDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        return String.format("%.1f", value);
    }

    private String formatTrend(double value) {
        String symbol = value > 0 ? "↑" : (value < 0 ? "↓" : "→");
        return symbol + formatDouble(Math.abs(value)) + "%";
    }
}
