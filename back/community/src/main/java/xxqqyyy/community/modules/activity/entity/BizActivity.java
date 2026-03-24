package xxqqyyy.community.modules.activity.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 活动主表实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizActivity extends BaseAuditEntity {

    private String title;

    private String content;

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
}

