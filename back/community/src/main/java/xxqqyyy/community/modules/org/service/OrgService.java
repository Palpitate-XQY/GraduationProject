package xxqqyyy.community.modules.org.service;

import java.util.List;
import xxqqyyy.community.modules.org.dto.OrgCreateRequest;
import xxqqyyy.community.modules.org.dto.OrgQuery;
import xxqqyyy.community.modules.org.dto.OrgUpdateRequest;
import xxqqyyy.community.modules.org.vo.OrgTreeVO;

/**
 * 组织管理服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface OrgService {

    List<OrgTreeVO> tree(OrgQuery query);

    void create(OrgCreateRequest request);

    void update(OrgUpdateRequest request);

    void delete(Long orgId);
}

