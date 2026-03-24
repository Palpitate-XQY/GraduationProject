package xxqqyyy.community.modules.org.enums;

import lombok.Getter;

/**
 * 组织类型枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum OrgTypeEnum {
    STREET("STREET", "街道"),
    COMMUNITY("COMMUNITY", "社区"),
    COMPLEX("COMPLEX", "小区"),
    PROPERTY_COMPANY("PROPERTY_COMPANY", "物业公司"),
    DEPARTMENT("DEPARTMENT", "部门");

    private final String code;
    private final String desc;

    OrgTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

