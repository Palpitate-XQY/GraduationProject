package xxqqyyy.community.modules.repair.enums;

import lombok.Getter;

/**
 * 报修附件类型枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum RepairAttachmentTypeEnum {
    REPORT("REPORT", "报修图片"),
    PROCESS("PROCESS", "处理过程附件"),
    RESULT("RESULT", "处理结果附件"),
    FEEDBACK("FEEDBACK", "未解决反馈附件");

    private final String code;
    private final String desc;

    RepairAttachmentTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
