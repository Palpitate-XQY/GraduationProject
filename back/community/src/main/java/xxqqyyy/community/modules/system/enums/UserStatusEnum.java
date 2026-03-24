package xxqqyyy.community.modules.system.enums;

import lombok.Getter;

/**
 * 用户状态枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum UserStatusEnum {
    ENABLED(1, "启用"),
    DISABLED(0, "禁用");

    private final int code;
    private final String desc;

    UserStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

