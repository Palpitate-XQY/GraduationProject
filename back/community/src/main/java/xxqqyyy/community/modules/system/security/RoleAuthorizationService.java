package xxqqyyy.community.modules.system.security;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import xxqqyyy.community.modules.system.entity.SysRole;
import xxqqyyy.community.modules.system.mapper.SysRoleMapper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * Authorization extension for role creation.
 * Allows users with child-role creation capability to access create flow.
 */
@Component("roleAuthorizationService")
@RequiredArgsConstructor
public class RoleAuthorizationService {

    private static final String ROLE_CREATE_PERMISSION = "sys:role:create";

    private final SysRoleMapper sysRoleMapper;

    public boolean canCreateRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginPrincipal principal)) {
            return false;
        }
        if (principal.isSuperAdmin()) {
            return true;
        }
        Set<String> permissionCodes = principal.getPermissionCodes() == null
            ? Collections.emptySet()
            : principal.getPermissionCodes();
        if (permissionCodes.contains(ROLE_CREATE_PERMISSION)) {
            return true;
        }
        List<SysRole> roles = sysRoleMapper.selectEnabledByUserId(principal.getUserId());
        return roles.stream().anyMatch(role -> role.getAllowCreateChild() != null && role.getAllowCreateChild() == 1);
    }
}