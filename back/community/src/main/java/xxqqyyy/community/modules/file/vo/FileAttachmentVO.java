package xxqqyyy.community.modules.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 业务附件视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "业务附件信息")
public class FileAttachmentVO {

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "原始文件名")
    private String originFileName;

    @Schema(description = "系统文件名")
    private String fileName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件MIME类型")
    private String contentType;

    @Schema(description = "存储类型：LOCAL/QINIU")
    private String storageType;

    @Schema(description = "访问地址")
    private String accessUrl;
}

