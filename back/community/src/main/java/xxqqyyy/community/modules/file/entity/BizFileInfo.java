package xxqqyyy.community.modules.file.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 文件信息实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizFileInfo extends BaseAuditEntity {

    private String bizType;

    private Long bizId;

    private String storageType;

    private String fileName;

    private String originFileName;

    private String filePath;

    private Long fileSize;

    private String contentType;

    private Long uploaderId;
}

