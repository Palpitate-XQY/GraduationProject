package xxqqyyy.community.modules.system.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.org.entity.BizComplexPropertyRel;
import xxqqyyy.community.modules.org.mapper.BizComplexPropertyRelMapper;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.system.entity.SysRoleScope;
import xxqqyyy.community.modules.system.enums.DataScopeTypeEnum;
import xxqqyyy.community.modules.system.mapper.SysRoleMapper;
import xxqqyyy.community.modules.system.mapper.SysRoleScopeMapper;
import xxqqyyy.community.modules.system.model.DataScopeResult;
import xxqqyyy.community.modules.system.service.DataScopeService;

/**
 * 数据范围服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class DataScopeServiceImpl implements DataScopeService {

    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleScopeMapper sysRoleScopeMapper;
    private final SysOrgMapper sysOrgMapper;
    private final BizComplexPropertyRelMapper bizComplexPropertyRelMapper;

    @Override
    public DataScopeResult resolveByUserId(Long userId) {
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(userId);
        if (roleCodes.stream().anyMatch(code -> SUPER_ADMIN_ROLE_CODE.equalsIgnoreCase(code))) {
            return DataScopeResult.builder().allAccess(true).build();
        }
        List<Long> roleIds = sysRoleMapper.selectRoleIdsByUserId(userId);
        List<SysRoleScope> scopes = sysRoleScopeMapper.selectByRoleIds(roleIds);
        Set<Long> orgIds = new HashSet<>();
        for (SysRoleScope scope : scopes) {
            String scopeType = scope.getScopeType();
            if (DataScopeTypeEnum.ALL.getCode().equalsIgnoreCase(scopeType)) {
                return DataScopeResult.builder().allAccess(true).build();
            }
            Long refId = scope.getScopeRefId();
            if (DataScopeTypeEnum.SELF.getCode().equalsIgnoreCase(scopeType) || refId == null) {
                continue;
            }
            orgIds.add(refId);
            if (DataScopeTypeEnum.PROPERTY_COMPANY.getCode().equalsIgnoreCase(scopeType)) {
                List<BizComplexPropertyRel> relList = bizComplexPropertyRelMapper.selectByPropertyCompanyOrgId(refId);
                for (BizComplexPropertyRel rel : relList) {
                    if (rel.getStatus() == null || rel.getStatus() != 1 || rel.getComplexOrgId() == null) {
                        continue;
                    }
                    orgIds.add(rel.getComplexOrgId());
                }
            }
            if (DataScopeTypeEnum.STREET.getCode().equalsIgnoreCase(scopeType)
                || DataScopeTypeEnum.COMMUNITY.getCode().equalsIgnoreCase(scopeType)
                || DataScopeTypeEnum.COMPLEX.getCode().equalsIgnoreCase(scopeType)
                || DataScopeTypeEnum.CUSTOM.getCode().equalsIgnoreCase(scopeType)) {
                orgIds.addAll(sysOrgMapper.selectDescendantIds(refId));
            }
        }
        return DataScopeResult.builder().allAccess(false).orgIds(orgIds).build();
    }

    @Override
    public void assertOrgAccessible(Long userId, Long targetOrgId) {
        if (targetOrgId == null) {
            return;
        }
        DataScopeResult result = resolveByUserId(userId);
        if (result.isAllAccess()) {
            return;
        }
        if (!result.getSafeOrgIds().contains(targetOrgId)) {
            throw new BizException(ErrorCode.DATA_SCOPE_DENIED, "无权访问目标组织");
        }
    }

    @Override
    public void assertRoleScopeGrantable(Long creatorUserId, Set<String> scopeTypes, Set<Long> scopeRefIds) {
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(creatorUserId);
        if (roleCodes.stream().anyMatch(code -> SUPER_ADMIN_ROLE_CODE.equalsIgnoreCase(code))) {
            return;
        }
        if (scopeTypes.stream().anyMatch(type -> DataScopeTypeEnum.ALL.getCode().equalsIgnoreCase(type))) {
            throw new BizException(ErrorCode.DATA_SCOPE_DENIED, "非超级管理员不可分配 ALL 数据范围");
        }
        DataScopeResult creatorScope = resolveByUserId(creatorUserId);
        Set<Long> allowOrgIds = creatorScope.getSafeOrgIds();
        for (Long scopeRefId : scopeRefIds) {
            if (scopeRefId == null) {
                continue;
            }
            if (!allowOrgIds.contains(scopeRefId)) {
                throw new BizException(ErrorCode.DATA_SCOPE_DENIED, "角色数据范围越权下发");
            }
        }
        boolean hasInvalidScopeType = scopeTypes.stream().filter(Objects::nonNull).anyMatch(type -> !DataScopeTypeEnum.valid(type));
        if (hasInvalidScopeType) {
            throw new BizException(ErrorCode.BAD_REQUEST, "存在非法数据范围类型");
        }
    }
}
