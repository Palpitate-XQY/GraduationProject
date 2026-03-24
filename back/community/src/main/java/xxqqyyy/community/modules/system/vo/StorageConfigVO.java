package xxqqyyy.community.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 存储配置返回对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "存储配置")
public class StorageConfigVO {

    @Schema(description = "存储类型：LOCAL/QINIU")
    private String storageType;

    @Schema(description = "本地存储根目录")
    private String localBasePath;

    @Schema(description = "本地访问前缀")
    private String localAccessBaseUrl;

    @Schema(description = "七牛 AccessKey")
    private String qiniuAccessKey;

    @Schema(description = "七牛 SecretKey（脱敏）")
    private String qiniuSecretKeyMasked;

    @Schema(description = "七牛 Bucket")
    private String qiniuBucket;

    @Schema(description = "七牛 Region")
    private String qiniuRegion;

    @Schema(description = "七牛访问域名")
    private String qiniuDomain;

    @Schema(description = "备注")
    private String remark;
}

