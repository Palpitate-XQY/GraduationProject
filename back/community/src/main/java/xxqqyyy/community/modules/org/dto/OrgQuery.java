package xxqqyyy.community.modules.org.dto;

import lombok.Data;

/**
 * 组织查询参数。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
public class OrgQuery {

    private String keyword;

    private String orgType;

    private Integer status;
}

