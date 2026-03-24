package xxqqyyy.community.modules.system.service;

import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.system.dto.UserCreateRequest;
import xxqqyyy.community.modules.system.dto.UserPageQuery;
import xxqqyyy.community.modules.system.dto.UserRoleAssignRequest;
import xxqqyyy.community.modules.system.dto.UserUpdateRequest;
import xxqqyyy.community.modules.system.vo.UserVO;

/**
 * 用户管理服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface UserService {

    PageResult<UserVO> page(UserPageQuery query);

    UserVO getById(Long userId);

    void create(UserCreateRequest request);

    void update(UserUpdateRequest request);

    void delete(Long userId);

    void updateStatus(Long userId, Integer status);

    void assignRoles(UserRoleAssignRequest request);
}

