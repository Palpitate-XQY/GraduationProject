package xxqqyyy.community.modules.activity.enums;

import lombok.Getter;

/**
 * 活动状态枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum ActivityStatusEnum {
    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    RECALLED("RECALLED", "已撤回"),
    FINISHED("FINISHED", "已结束");

    private final String code;
    private final String desc;

    ActivityStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

