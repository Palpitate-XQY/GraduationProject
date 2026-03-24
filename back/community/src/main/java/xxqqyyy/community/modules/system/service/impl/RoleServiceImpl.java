package xxqqyyy.community.modules.system.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.system.dto.RoleCreateRequest;
import xxqqyyy.community.modules.system.dto.RoleScopeConfigItem;
import xxqqyyy.community.modules.system.dto.RoleUpdateRequest;
import xxqqyyy.community.modules.system.entity.SysRole;
import xxqqyyy.community.modules.system.entity.SysRoleScope;
import xxqqyyy.community.modules.system.mapper.SysPermissionMapper;
import xxqqyyy.community.modules.system.mapper.SysRoleMapper;
import xxqqyyy.community.modules.system.mapper.SysRolePermissionMapper;
import xxqqyyy.community.modules.system.mapper.SysRoleScopeMapper;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.modules.system.service.RoleService;
import xxqqyyy.community.modules.system.vo.RoleVO;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 角色管理服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysRoleScopeMapper sysRoleScopeMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final DataScopeService dataScopeService;

    @Override
    public PageResult<RoleVO> page(String keyword, long current, long size) {
        long total = sysRoleMapper.countPage(keyword);
        if (total == 0) {
            return PageResult.empty(current, size);
        }
        long offset = (current - 1) * size;
        List<RoleVO> records = sysRoleMapper.selectPage(keyword, offset, size).stream()
            .map(this::toRoleVO).toList();
        return PageResult.<RoleVO>builder()
            .records(records)
            .total(total)
            .current(current)
            .size(size)
            .build();
    }

    @Override
    public RoleVO getById(Long roleId) {
        SysRole role = requireRole(roleId);
        return toRoleVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(RoleCreateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        validatePermissionGrant(principal, request.getPermissionIds());
        validateScopeGrant(principal, request.getScopeConfigs());
        if (sysRoleMapper.countByCode(request.getRoleCode(), null) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "角色编码已存在");
        }
        if (request.getParentRoleId() != null) {
            SysRole parentRole = requireRole(request.getParentRoleId());
            if (parentRole.getAllowCreateChild() == null || parentRole.getAllowCreateChild() == 0) {
                throw new BizException(ErrorCode.BIZ_ERROR, "父角色不允许创建下级角色");
            }
        }
        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setParentRoleId(request.getParentRoleId());
        role.setAllowCreateChild(request.getAllowCreateChild());
        role.setStatus(request.getStatus());
        role.setSort(request.getSort() == null ? 0 : request.getSort());
        role.setRemark(request.getRemark());
        role.setCreateBy(principal.getUserId());
        role.setUpdateBy(principal.getUserId());
        sysRoleMapper.insert(role);
        saveRolePermissionsAndScopes(role.getId(), request.getPermissionIds(), request.getScopeConfigs());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleUpdateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysRole role = requireRole(request.getId());
        validatePermissionGrant(principal, request.getPermissionIds());
        validateScopeGrant(principal, request.getScopeConfigs());
        role.setRoleName(request.getRoleName());
        role.setStatus(request.getStatus());
        role.setAllowCreateChild(request.getAllowCreateChild());
        role.setSort(request.getSort() == null ? 0 : request.getSort());
        role.setRemark(request.getRemark());
        role.setUpdateBy(principal.getUserId());
        sysRoleMapper.update(role);
        saveRolePermissionsAndScopes(role.getId(), request.getPermissionIds(), request.getScopeConfigs());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long roleId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        requireRole(roleId);
        sysRoleMapper.logicalDelete(roleId, principal.getUserId());
        sysRolePermissionMapper.deleteByRoleId(roleId);
        sysRoleScopeMapper.deleteByRoleId(roleId);
    }

    private void saveRolePermissionsAndScopes(Long roleId, Set<Long> permissionIds, Set<RoleScopeConfigItem> scopeConfigs) {
        sysRolePermissionMapper.deleteByRoleId(roleId);
        if (!CollectionUtils.isEmpty(permissionIds)) {
            sysRolePermissionMapper.batchInsert(roleId, permissionIds);
        }
        sysRoleScopeMapper.deleteByRoleId(roleId);
        if (!CollectionUtils.isEmpty(scopeConfigs)) {
            Set<SysRoleScope> scopes = scopeConfigs.stream().map(item -> {
                SysRoleScope scope = new SysRoleScope();
                scope.setRoleId(roleId);
                scope.setScopeType(item.getScopeType());
                scope.setScopeRefId(item.getScopeRefId());
                return scope;
            }).collect(java.util.stream.Collectors.toSet());
            sysRoleScopeMapper.batchInsert(scopes);
        }
    }

    private void validatePermissionGrant(LoginPrincipal principal, Set<Long> permissionIds) {
        if (principal.isSuperAdmin()) {
            return;
        }
        Set<String> requesterPermissions = principal.getPermissionCodes();
        Set<String> targetPermissionCodes = new HashSet<>(sysPermissionMapper.selectCodesByIds(permissionIds));
        if (!requesterPermissions.containsAll(targetPermissionCodes)) {
            throw new BizException(ErrorCode.FORBIDDEN, "下发权限超出当前用户权限范围");
        }
    }

    private void validateScopeGrant(LoginPrincipal principal, Set<RoleScopeConfigItem> scopeConfigs) {
        Set<String> scopeTypes = scopeConfigs.stream().map(RoleScopeConfigItem::getScopeType).collect(java.util.stream.Collectors.toSet());
        Set<Long> scopeRefIds = scopeConfigs.stream().map(RoleScopeConfigItem::getScopeRefId).collect(java.util.stream.Collectors.toSet());
        dataScopeService.assertRoleScopeGrantable(principal.getUserId(), scopeTypes, scopeRefIds);
    }

    private SysRole requireRole(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private RoleVO toRoleVO(SysRole role) {
        List<Long> permissionIds = sysRolePermissionMapper.selectPermissionIdsByRoleId(role.getId());
        List<SysRoleScope> scopes = sysRoleScopeMapper.selectByRoleId(role.getId());
        Set<RoleScopeConfigItem> scopeItems = scopes.stream().map(scope -> {
            RoleScopeConfigItem item = new RoleScopeConfigItem();
            item.setScopeType(scope.getScopeType());
            item.setScopeRefId(scope.getScopeRefId());
            return item;
        }).collect(java.util.stream.Collectors.toSet());
        return RoleVO.builder()
            .id(role.getId())
            .roleCode(role.getRoleCode())
            .roleName(role.getRoleName())
            .status(role.getStatus())
            .parentRoleId(role.getParentRoleId())
            .allowCreateChild(role.getAllowCreateChild())
            .sort(role.getSort())
            .remark(role.getRemark())
            .permissionIds(new HashSet<>(permissionIds))
            .scopeConfigs(scopeItems)
            .createTime(role.getCreateTime())
            .build();
    }
}

