package xxqqyyy.community.modules.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 存储配置更新请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "存储配置更新请求")
public class StorageConfigUpdateRequest {

    @NotBlank(message = "存储类型不能为空")
    @Schema(description = "存储类型：LOCAL/QINIU", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storageType;

    @Schema(description = "本地存储根目录")
    private String localBasePath;

    @Schema(description = "本地访问前缀")
    private String localAccessBaseUrl;

    @Schema(description = "七牛 AccessKey")
    private String qiniuAccessKey;

    @Schema(description = "七牛 SecretKey，为空时保持原值")
    private String qiniuSecretKey;

    @Schema(description = "七牛 Bucket")
    private String qiniuBucket;

    @Schema(description = "七牛 Region，如 z0/z1/z2/na0/as0/auto")
    private String qiniuRegion;

    @Schema(description = "七牛访问域名")
    private String qiniuDomain;

    @Schema(description = "备注")
    private String remark;
}

