package xxqqyyy.community.modules.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 文件信息返回对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "文件信息")
public class FileInfoVO {

    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "存储类型：LOCAL/QINIU")
    private String storageType;

    @Schema(description = "系统文件名")
    private String fileName;

    @Schema(description = "原始文件名")
    private String originFileName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件MIME类型")
    private String contentType;

    @Schema(description = "文件访问地址")
    private String accessUrl;

    @Schema(description = "上传人ID")
    private Long uploaderId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

