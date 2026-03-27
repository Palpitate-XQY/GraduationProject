package xxqqyyy.community.modules.analytics.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * Analytics report period type.
 */
@Getter
public enum AnalyticsReportTypeEnum {
    DAILY("DAILY"),
    WEEKLY("WEEKLY");

    private final String code;

    AnalyticsReportTypeEnum(String code) {
        this.code = code;
    }

    public static boolean valid(String code) {
        return Arrays.stream(values()).anyMatch(v -> v.code.equalsIgnoreCase(code));
    }

    public static AnalyticsReportTypeEnum fromCode(String code) {
        return Arrays.stream(values())
            .filter(v -> v.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}

