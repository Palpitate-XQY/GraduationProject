package xxqqyyy.community.modules.activity.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import xxqqyyy.community.modules.activity.dto.ActivityScopeItem;
import xxqqyyy.community.modules.file.vo.FileAttachmentVO;

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

    /**
     * 结构化附件列表（由 attachmentJson 解析得到）。
     */
    private List<FileAttachmentVO> attachments;

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

    /**
     * 当前登录用户是否已报名。
     */
    private Boolean signedByMe;

    /**
     * 当前登录用户报名状态：SIGNED / CANCELED。
     */
    private String signupStatus;

    /**
     * 当前登录用户最近一次报名时间。
     */
    private LocalDateTime mySignupTime;

    private Set<ActivityScopeItem> scopeItems;
}
