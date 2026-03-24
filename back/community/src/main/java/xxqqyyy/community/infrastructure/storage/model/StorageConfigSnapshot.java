package xxqqyyy.community.infrastructure.storage.model;

import lombok.Builder;
import lombok.Data;

/**
 * 存储配置快照，用于上传/下载执行期读取。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class StorageConfigSnapshot {

    private String storageType;

    private String localBasePath;

    private String localAccessBaseUrl;

    private String qiniuAccessKey;

    private String qiniuSecretKey;

    private String qiniuBucket;

    private String qiniuRegion;

    private String qiniuDomain;
}

