package xxqqyyy.community.modules.notice.enums;

import lombok.Getter;

/**
 * 公告类型枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum NoticeTypeEnum {
    STREET_COMMUNITY("STREET_COMMUNITY", "街道/社区公告"),
    PROPERTY("PROPERTY", "物业公告");

    private final String code;
    private final String desc;

    NoticeTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

