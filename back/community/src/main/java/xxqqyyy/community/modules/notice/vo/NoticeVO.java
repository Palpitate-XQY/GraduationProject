package xxqqyyy.community.modules.notice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import xxqqyyy.community.modules.notice.dto.NoticeScopeItem;

/**
 * 公告视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "公告信息")
public class NoticeVO {

    private Long id;

    private String noticeType;

    private String title;

    private String content;

    private Long coverFileId;

    private String attachmentJson;

    private String status;

    private Integer topFlag;

    private Long publisherOrgId;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    private Set<NoticeScopeItem> scopeItems;
}

