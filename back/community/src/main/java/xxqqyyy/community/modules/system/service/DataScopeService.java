package xxqqyyy.community.modules.system.service;

import java.util.Set;
import xxqqyyy.community.modules.system.model.DataScopeResult;

/**
 * 数据范围服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface DataScopeService {

    /**
     * 计算用户可访问的数据范围。
     *
     * @param userId 用户ID
     * @return 数据范围结果
     */
    DataScopeResult resolveByUserId(Long userId);

    /**
     * 校验目标组织ID是否在用户可访问范围内。
     *
     * @param userId 用户ID
     * @param targetOrgId 目标组织ID
     */
    void assertOrgAccessible(Long userId, Long targetOrgId);

    /**
     * 校验角色数据范围是否越权。
     *
     * @param creatorUserId 创建者用户ID
     * @param scopeTypes 范围类型集合
     * @param scopeRefIds 范围引用组织ID集合
     */
    void assertRoleScopeGrantable(Long creatorUserId, Set<String> scopeTypes, Set<Long> scopeRefIds);
}

