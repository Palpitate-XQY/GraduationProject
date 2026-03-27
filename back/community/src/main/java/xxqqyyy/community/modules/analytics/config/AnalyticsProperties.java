package xxqqyyy.community.modules.analytics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Analytics module configuration properties.
 */
@Data
@ConfigurationProperties(prefix = "community.analytics")
public class AnalyticsProperties {

    /**
     * Master switch of analytics module.
     */
    private boolean enabled = true;

    /**
     * Maximum text records fetched from each data source during one generation.
     */
    private int maxCorpusPerSource = 300;

    /**
     * Word cloud output count limit.
     */
    private int wordCloudTopN = 80;

    /**
     * Minimum word frequency retained in word cloud.
     */
    private int minWordFrequency = 2;

    private Report report = new Report();

    @Data
    public static class Report {

        /**
         * Daily report cron expression.
         */
        private String dailyCron = "0 30 8 * * ?";

        /**
         * Weekly report cron expression.
         */
        private String weeklyCron = "0 0 9 ? * MON";

        /**
         * Time zone used by analytics schedule and period calculation.
         */
        private String zone = "Asia/Shanghai";
    }
}
