package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.system.dto.UserRoleAssignRequest;
import xxqqyyy.community.modules.system.entity.SysRole;
import xxqqyyy.community.modules.system.entity.SysUser;
import xxqqyyy.community.modules.system.mapper.SysPermissionMapper;
import xxqqyyy.community.modules.system.mapper.SysRoleMapper;
import xxqqyyy.community.modules.system.mapper.SysRolePermissionMapper;
import xxqqyyy.community.modules.system.mapper.SysRoleScopeMapper;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
import xxqqyyy.community.modules.system.mapper.SysUserRoleMapper;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.modules.system.service.impl.UserServiceImpl;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 用户角色分配越权测试。
 *
 * @author codex
 * @since 1.0.0
 */
class UserRoleGrantTest {

    private SysUserMapper sysUserMapper;
    private SysUserRoleMapper sysUserRoleMapper;
    private SysRoleMapper sysRoleMapper;
    private SysRolePermissionMapper sysRolePermissionMapper;
    private SysRoleScopeMapper sysRoleScopeMapper;
    private SysPermissionMapper sysPermissionMapper;
    private SysOrgMapper sysOrgMapper;
    private DataScopeService dataScopeService;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        sysUserMapper = mock(SysUserMapper.class);
        sysUserRoleMapper = mock(SysUserRoleMapper.class);
        sysRoleMapper = mock(SysRoleMapper.class);
        sysRolePermissionMapper = mock(SysRolePermissionMapper.class);
        sysRoleScopeMapper = mock(SysRoleScopeMapper.class);
        sysPermissionMapper = mock(SysPermissionMapper.class);
        sysOrgMapper = mock(SysOrgMapper.class);
        dataScopeService = mock(DataScopeService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserServiceImpl(
            sysUserMapper,
            sysUserRoleMapper,
            sysRoleMapper,
            sysRolePermissionMapper,
            sysRoleScopeMapper,
            sysPermissionMapper,
            sysOrgMapper,
            dataScopeService,
            passwordEncoder
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldDenyAssignRoleWhenPermissionOutOfRange() {
        LoginPrincipal principal = LoginPrincipal.builder()
            .userId(1L)
            .username("community_admin")
            .orgId(2L)
            .permissionCodes(Set.of("sys:user:list"))
            .roleCodes(Set.of("COMMUNITY_ADMIN"))
            .superAdmin(false)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        SysUser targetUser = new SysUser();
        targetUser.setId(9L);
        targetUser.setOrgId(2L);
        when(sysUserMapper.selectById(9L)).thenReturn(targetUser);
        doNothing().when(dataScopeService).assertOrgAccessible(1L, 2L);

        SysRole role = new SysRole();
        role.setId(100L);
        role.setStatus(1);
        when(sysRoleMapper.selectByIds(Set.of(100L))).thenReturn(List.of(role));
        when(sysRolePermissionMapper.selectPermissionIdsByRoleId(100L)).thenReturn(List.of(200L));
        when(sysPermissionMapper.selectCodesByIds(Set.of(200L))).thenReturn(List.of("sys:user:create"));

        UserRoleAssignRequest request = new UserRoleAssignRequest();
        request.setUserId(9L);
        request.setRoleIds(Set.of(100L));

        assertThrows(BizException.class, () -> userService.assignRoles(request));
    }
}
