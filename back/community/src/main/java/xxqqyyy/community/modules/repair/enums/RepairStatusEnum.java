package xxqqyyy.community.modules.repair.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * 报修状态枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum RepairStatusEnum {
    PENDING_ACCEPT("PENDING_ACCEPT", "待受理"),
    ACCEPTED("ACCEPTED", "已受理"),
    ASSIGNED("ASSIGNED", "已分派"),
    PROCESSING("PROCESSING", "处理中"),
    WAIT_CONFIRM("WAIT_CONFIRM", "待确认"),
    DONE("DONE", "已完成"),
    CLOSED("CLOSED", "已关闭"),
    REJECTED("REJECTED", "已驳回"),
    REOPENED("REOPENED", "重新处理");

    private final String code;
    private final String desc;

    RepairStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 判断状态编码是否合法。
     *
     * @param code 状态编码
     * @return 是否合法
     */
    public static boolean valid(String code) {
        return Arrays.stream(values()).anyMatch(v -> v.code.equalsIgnoreCase(code));
    }

    /**
     * 根据编码转换枚举，不存在则返回 null。
     *
     * @param code 状态编码
     * @return 状态枚举
     */
    public static RepairStatusEnum fromCode(String code) {
        return Arrays.stream(values())
            .filter(v -> v.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
