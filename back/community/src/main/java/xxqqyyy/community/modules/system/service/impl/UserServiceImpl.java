package xxqqyyy.community.modules.system.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.org.entity.SysOrg;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.system.dto.UserCreateRequest;
import xxqqyyy.community.modules.system.dto.UserPageQuery;
import xxqqyyy.community.modules.system.dto.UserRoleAssignRequest;
import xxqqyyy.community.modules.system.dto.UserUpdateRequest;
import xxqqyyy.community.modules.system.entity.SysUser;
import xxqqyyy.community.modules.system.mapper.SysRoleMapper;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
import xxqqyyy.community.modules.system.mapper.SysUserRoleMapper;
import xxqqyyy.community.modules.system.model.DataScopeResult;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.modules.system.service.UserService;
import xxqqyyy.community.modules.system.vo.UserVO;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 用户管理服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysOrgMapper sysOrgMapper;
    private final DataScopeService dataScopeService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> page(UserPageQuery query) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        DataScopeResult scope = dataScopeService.resolveByUserId(principal.getUserId());
        long total = sysUserMapper.countPage(query, scope.isAllAccess(), scope.getSafeOrgIds());
        if (total == 0) {
            return PageResult.empty(query.getCurrent(), query.getSize());
        }
        long offset = (query.getCurrent() - 1) * query.getSize();
        List<SysUser> users = sysUserMapper.selectPage(query, scope.isAllAccess(), scope.getSafeOrgIds(), offset, query.getSize());
        Set<Long> orgIds = users.stream().map(SysUser::getOrgId).collect(Collectors.toSet());
        Map<Long, String> orgNameMap = buildOrgNameMap(orgIds);
        List<UserVO> records = users.stream().map(user -> UserVO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .phone(user.getPhone())
            .email(user.getEmail())
            .status(user.getStatus())
            .orgId(user.getOrgId())
            .orgName(orgNameMap.get(user.getOrgId()))
            .roleIds(Set.copyOf(sysUserRoleMapper.selectRoleIdsByUserId(user.getId())))
            .roleCodes(Set.copyOf(sysRoleMapper.selectRoleCodesByUserId(user.getId())))
            .createTime(user.getCreateTime())
            .build()).toList();
        return PageResult.<UserVO>builder()
            .records(records)
            .total(total)
            .current(query.getCurrent())
            .size(query.getSize())
            .build();
    }

    @Override
    public UserVO getById(Long userId) {
        SysUser user = requireUser(userId);
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        dataScopeService.assertOrgAccessible(principal.getUserId(), user.getOrgId());
        String orgName = null;
        SysOrg org = sysOrgMapper.selectById(user.getOrgId());
        if (org != null) {
            orgName = org.getOrgName();
        }
        return UserVO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .phone(user.getPhone())
            .email(user.getEmail())
            .status(user.getStatus())
            .orgId(user.getOrgId())
            .orgName(orgName)
            .roleIds(Set.copyOf(sysUserRoleMapper.selectRoleIdsByUserId(userId)))
            .roleCodes(Set.copyOf(sysRoleMapper.selectRoleCodesByUserId(userId)))
            .createTime(user.getCreateTime())
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(UserCreateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        dataScopeService.assertOrgAccessible(principal.getUserId(), request.getOrgId());
        if (sysUserMapper.countByUsername(request.getUsername(), null) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setOrgId(request.getOrgId());
        user.setStatus(request.getStatus());
        user.setMustChangePassword(1);
        user.setCreateBy(principal.getUserId());
        user.setUpdateBy(principal.getUserId());
        sysUserMapper.insert(user);
        assignRolesInternal(user.getId(), request.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserUpdateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysUser user = requireUser(request.getId());
        dataScopeService.assertOrgAccessible(principal.getUserId(), user.getOrgId());
        dataScopeService.assertOrgAccessible(principal.getUserId(), request.getOrgId());
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());
        user.setOrgId(request.getOrgId());
        user.setUpdateBy(principal.getUserId());
        sysUserMapper.update(user);
        assignRolesInternal(user.getId(), request.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysUser user = requireUser(userId);
        dataScopeService.assertOrgAccessible(principal.getUserId(), user.getOrgId());
        sysUserMapper.logicalDelete(userId, principal.getUserId());
        sysUserRoleMapper.deleteByUserId(userId);
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysUser user = requireUser(userId);
        dataScopeService.assertOrgAccessible(principal.getUserId(), user.getOrgId());
        sysUserMapper.updateStatus(userId, status, principal.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(UserRoleAssignRequest request) {
        SysUser user = requireUser(request.getUserId());
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        dataScopeService.assertOrgAccessible(principal.getUserId(), user.getOrgId());
        assignRolesInternal(request.getUserId(), request.getRoleIds());
    }

    private void assignRolesInternal(Long userId, Set<Long> roleIds) {
        sysUserRoleMapper.deleteByUserId(userId);
        if (!CollectionUtils.isEmpty(roleIds)) {
            sysUserRoleMapper.batchInsert(userId, roleIds);
        }
    }

    private SysUser requireUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private Map<Long, String> buildOrgNameMap(Set<Long> orgIds) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return Collections.emptyMap();
        }
        List<SysOrg> orgs = sysOrgMapper.selectByIds(orgIds);
        Map<Long, String> map = new HashMap<>(orgs.size());
        for (SysOrg org : orgs) {
            map.put(org.getId(), org.getOrgName());
        }
        return map;
    }
}

