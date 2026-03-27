package xxqqyyy.community.modules.analytics.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * Scope kind used by analytics report cache key.
 */
@Getter
public enum AnalyticsScopeKindEnum {
    ALL("ALL"),
    STREET("STREET"),
    COMMUNITY("COMMUNITY"),
    COMPLEX("COMPLEX"),
    PROPERTY_COMPANY("PROPERTY_COMPANY"),
    SELF("SELF"),
    CUSTOM("CUSTOM");

    private final String code;

    AnalyticsScopeKindEnum(String code) {
        this.code = code;
    }

    public static boolean valid(String code) {
        return Arrays.stream(values()).anyMatch(v -> v.code.equalsIgnoreCase(code));
    }
}

