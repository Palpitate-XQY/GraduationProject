package xxqqyyy.community.modules.org.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * 组织树节点视图。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class OrgTreeVO {

    private Long id;

    private Long parentId;

    private String orgCode;

    private String orgName;

    private String orgType;

    private Integer status;

    private Integer sort;

    @Builder.Default
    private List<OrgTreeVO> children = new ArrayList<>();
}

