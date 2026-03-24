package xxqqyyy.community.modules.system.service;

import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.system.dto.PermissionCreateRequest;
import xxqqyyy.community.modules.system.dto.PermissionUpdateRequest;
import xxqqyyy.community.modules.system.vo.PermissionVO;

/**
 * 权限管理服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface PermissionService {

    PageResult<PermissionVO> page(String keyword, long current, long size);

    PermissionVO getById(Long permissionId);

    void create(PermissionCreateRequest request);

    void update(PermissionUpdateRequest request);

    void delete(Long permissionId);
}

