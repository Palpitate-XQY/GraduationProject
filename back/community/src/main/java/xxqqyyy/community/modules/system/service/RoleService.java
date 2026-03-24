package xxqqyyy.community.modules.system.service;

import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.system.dto.RoleCreateRequest;
import xxqqyyy.community.modules.system.dto.RoleUpdateRequest;
import xxqqyyy.community.modules.system.vo.RoleVO;

/**
 * 角色管理服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface RoleService {

    PageResult<RoleVO> page(String keyword, long current, long size);

    RoleVO getById(Long roleId);

    void create(RoleCreateRequest request);

    void update(RoleUpdateRequest request);

    void delete(Long roleId);
}

