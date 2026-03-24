package xxqqyyy.community.modules.system.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * 数据范围类型枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum DataScopeTypeEnum {
    ALL("ALL", "全部数据"),
    STREET("STREET", "街道范围"),
    COMMUNITY("COMMUNITY", "社区范围"),
    COMPLEX("COMPLEX", "小区范围"),
    PROPERTY_COMPANY("PROPERTY_COMPANY", "物业公司范围"),
    CUSTOM("CUSTOM", "自定义范围"),
    SELF("SELF", "本人数据");

    private final String code;
    private final String desc;

    DataScopeTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 判断 code 是否是合法数据范围。
     *
     * @param code 范围编码
     * @return 是否合法
     */
    public static boolean valid(String code) {
        return Arrays.stream(values()).anyMatch(v -> v.code.equalsIgnoreCase(code));
    }
}

