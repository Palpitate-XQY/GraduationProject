package xxqqyyy.community.modules.analytics.enums;

import lombok.Getter;

/**
 * AI generation mode marker.
 */
@Getter
public enum AnalyticsAiModeEnum {
    AI("AI"),
    FALLBACK("FALLBACK");

    private final String code;

    AnalyticsAiModeEnum(String code) {
        this.code = code;
    }
}

