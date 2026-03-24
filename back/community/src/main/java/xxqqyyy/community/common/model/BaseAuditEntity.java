package xxqqyyy.community.common.model;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 通用审计字段基类。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class BaseAuditEntity {

    private Long id;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记，0=未删除，1=已删除。
     */
    private Integer deleted;

    /**
     * 乐观锁版本号。
     */
    private Integer version;
}

