package xxqqyyy.community.modules.notice.enums;

import lombok.Getter;

/**
 * 公告状态枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum NoticeStatusEnum {
    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    RECALLED("RECALLED", "已撤回");

    private final String code;
    private final String desc;

    NoticeStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

