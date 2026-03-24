package xxqqyyy.community.modules.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.Data;

/**
 * 公告新增请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "公告新增请求")
public class NoticeCreateRequest {

    @NotBlank(message = "公告类型不能为空")
    @Schema(description = "公告类型：STREET_COMMUNITY/PROPERTY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String noticeType;

    @NotBlank(message = "标题不能为空")
    @Schema(description = "公告标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "公告内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "封面文件ID")
    private Long coverFileId;

    @Schema(description = "附件信息JSON")
    private String attachmentJson;

    @Schema(description = "是否置顶：1是，0否")
    private Integer topFlag;

    @Valid
    @NotEmpty(message = "可见范围不能为空")
    @Schema(description = "可见范围配置", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<NoticeScopeItem> scopeItems;
}

