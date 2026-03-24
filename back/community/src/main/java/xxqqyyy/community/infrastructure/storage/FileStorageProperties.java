package xxqqyyy.community.infrastructure.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储基础配置。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "community.file-storage")
public class FileStorageProperties {

    /**
     * 默认存储方式，数据库未配置时生效。
     */
    private String defaultType = "LOCAL";

    /**
     * 本地存储根目录，支持相对路径。
     */
    private String localBasePath = "./storage";

    /**
     * 本地存储访问前缀，当前实现仅作配置透传。
     */
    private String localAccessBaseUrl = "/api/files";

    /**
     * 文件上传大小上限（字节）。
     */
    private long maxFileSizeBytes = 20 * 1024 * 1024;
}

