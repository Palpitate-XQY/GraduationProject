package xxqqyyy.community.modules.system.enums;

import lombok.Getter;

/**
 * 角色状态枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum RoleStatusEnum {
    ENABLED(1, "启用"),
    DISABLED(0, "禁用");

    private final int code;
    private final String desc;

    RoleStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

