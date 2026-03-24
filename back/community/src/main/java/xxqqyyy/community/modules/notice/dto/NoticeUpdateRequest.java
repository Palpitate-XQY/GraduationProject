package xxqqyyy.community.modules.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

/**
 * 公告更新请求。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "公告更新请求")
public class NoticeUpdateRequest {

    @NotNull(message = "公告ID不能为空")
    private Long id;

    @NotBlank(message = "公告类型不能为空")
    private String noticeType;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long coverFileId;

    private String attachmentJson;

    private Integer topFlag;

    @Valid
    @NotEmpty(message = "可见范围不能为空")
    private Set<NoticeScopeItem> scopeItems;
}

