package xxqqyyy.community.modules.auth.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xxqqyyy.community.modules.system.entity.SysUser;
import xxqqyyy.community.modules.system.enums.UserStatusEnum;
import xxqqyyy.community.modules.system.mapper.SysPermissionMapper;
import xxqqyyy.community.modules.system.mapper.SysRoleMapper;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
import xxqqyyy.community.security.model.LoginPrincipal;
import xxqqyyy.community.security.service.SecurityPrincipalService;

/**
 * 登录主体加载实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AuthSecurityPrincipalServiceImpl implements SecurityPrincipalService {

    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public LoginPrincipal loadByUserId(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != UserStatusEnum.ENABLED.getCode()) {
            return null;
        }
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(userId);
        List<String> permissionCodes = sysPermissionMapper.selectPermissionCodesByUserId(userId);
        Set<String> roleCodeSet = new HashSet<>(roleCodes);
        boolean superAdmin = roleCodeSet.stream().anyMatch(code -> SUPER_ADMIN_ROLE_CODE.equalsIgnoreCase(code));
        return LoginPrincipal.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .nickname(user.getNickname())
            .orgId(user.getOrgId())
            .roleCodes(roleCodeSet)
            .permissionCodes(new HashSet<>(permissionCodes))
            .superAdmin(superAdmin)
            .build();
    }
}

