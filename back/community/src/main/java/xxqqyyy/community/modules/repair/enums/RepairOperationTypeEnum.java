package xxqqyyy.community.modules.repair.enums;

import lombok.Getter;

/**
 * 报修流转操作类型枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum RepairOperationTypeEnum {
    CREATE("CREATE", "创建工单"),
    ACCEPT("ACCEPT", "受理工单"),
    REJECT("REJECT", "驳回工单"),
    ASSIGN("ASSIGN", "分派工单"),
    TAKE("TAKE", "接单"),
    PROCESS("PROCESS", "处理工单"),
    SUBMIT("SUBMIT", "提交处理结果"),
    CONFIRM("CONFIRM", "居民确认解决"),
    REOPEN("REOPEN", "居民反馈未解决"),
    EVALUATE("EVALUATE", "居民评价"),
    URGE("URGE", "催单"),
    CLOSE("CLOSE", "关闭工单");

    private final String code;
    private final String desc;

    RepairOperationTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
