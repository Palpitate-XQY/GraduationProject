package xxqqyyy.community.modules.activity.vo;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import xxqqyyy.community.modules.activity.dto.ActivityScopeItem;

/**
 * 活动视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class ActivityVO {

    private Long id;

    private String title;

    private String content;

    /**
     * Markdown 渲染后的 HTML 内容。
     */
    private String contentHtml;

    private Long coverFileId;

    private String attachmentJson;

    private LocalDateTime activityStartTime;

    private LocalDateTime activityEndTime;

    private LocalDateTime signupStartTime;

    private LocalDateTime signupEndTime;

    private String location;

    private Integer maxParticipants;

    private String status;

    private Long publisherOrgId;

    private LocalDateTime publishTime;

    private LocalDateTime createTime;

    private Set<ActivityScopeItem> scopeItems;
}
