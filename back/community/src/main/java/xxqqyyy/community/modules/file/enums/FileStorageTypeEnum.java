package xxqqyyy.community.modules.file.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * 文件存储类型枚举。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum FileStorageTypeEnum {
    LOCAL("LOCAL", "本地存储"),
    QINIU("QINIU", "七牛云对象存储");

    private final String code;
    private final String desc;

    FileStorageTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 判断存储类型编码是否合法。
     *
     * @param code 存储类型编码
     * @return 是否合法
     */
    public static boolean valid(String code) {
        return Arrays.stream(values()).anyMatch(item -> item.code.equalsIgnoreCase(code));
    }

    /**
     * 按编码解析存储类型。
     *
     * @param code 存储类型编码
     * @return 枚举值，解析失败返回 null
     */
    public static FileStorageTypeEnum fromCode(String code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}

