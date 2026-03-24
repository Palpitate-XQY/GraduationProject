package xxqqyyy.community.infrastructure.storage.model;

import lombok.Builder;
import lombok.Data;

/**
 * 文件存储执行结果。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class StoredFileResult {

    /**
     * 存储中的对象路径（本地相对路径或云端 key）。
     */
    private String filePath;
}

