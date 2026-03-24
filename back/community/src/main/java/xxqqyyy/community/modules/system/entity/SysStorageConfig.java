package xxqqyyy.community.modules.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 文件存储配置实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysStorageConfig extends BaseAuditEntity {

    private String storageType;

    private String localBasePath;

    private String localAccessBaseUrl;

    private String qiniuAccessKey;

    private String qiniuSecretKey;

    private String qiniuBucket;

    private String qiniuRegion;

    private String qiniuDomain;

    private String remark;
}

