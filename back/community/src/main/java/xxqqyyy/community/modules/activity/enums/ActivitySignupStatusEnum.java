package xxqqyyy.community.modules.activity.enums;

import lombok.Getter;

/**
 * 活动报名状态枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum ActivitySignupStatusEnum {
    SIGNED("SIGNED", "已报名"),
    CANCELED("CANCELED", "已取消");

    private final String code;
    private final String desc;

    ActivitySignupStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

