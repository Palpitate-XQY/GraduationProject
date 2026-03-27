package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.system.dto.RoleCreateRequest;
import xxqqyyy.community.modules.system.dto.RoleScopeConfigItem;
import xxqqyyy.community.modules.system.entity.SysRole;
import xxqqyyy.community.modules.system.entity.SysRoleScope;
import xxqqyyy.community.modules.system.mapper.SysPermissionMapper;
import xxqqyyy.community.modules.system.mapper.SysRoleMapper;
import xxqqyyy.community.modules.system.mapper.SysRolePermissionMapper;
import xxqqyyy.community.modules.system.mapper.SysRoleScopeMapper;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.modules.system.service.impl.RoleServiceImpl;
import xxqqyyy.community.security.model.LoginPrincipal;

class RoleServiceCreateChildTest {

    private SysRoleMapper sysRoleMapper;
    private SysRolePermissionMapper sysRolePermissionMapper;
    private SysRoleScopeMapper sysRoleScopeMapper;
    private SysPermissionMapper sysPermissionMapper;
    private DataScopeService dataScopeService;
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        sysRoleMapper = mock(SysRoleMapper.class);
        sysRolePermissionMapper = mock(SysRolePermissionMapper.class);
        sysRoleScopeMapper = mock(SysRoleScopeMapper.class);
        sysPermissionMapper = mock(SysPermissionMapper.class);
        dataScopeService = mock(DataScopeService.class);
        roleService = new RoleServiceImpl(
            sysRoleMapper,
            sysRolePermissionMapper,
            sysRoleScopeMapper,
            sysPermissionMapper,
            dataScopeService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateChildRoleWhenParentOwnedAndAllowed() {
        setPrincipal(10L, Set.of("repair:assign"), false);

        SysRole parentRole = new SysRole();
        parentRole.setId(100L);
        parentRole.setStatus(1);
        parentRole.setAllowCreateChild(1);

        when(sysRoleMapper.selectById(100L)).thenReturn(parentRole);
        when(sysRoleMapper.selectRoleIdsByUserId(10L)).thenReturn(List.of(100L));
        when(sysRoleMapper.countByCode("REPAIR_WORKER", null)).thenReturn(0L);
        when(sysPermissionMapper.selectCodesByIds(Set.of(1L))).thenReturn(List.of("repair:assign"));
        doNothing().when(dataScopeService).assertRoleScopeGrantable(eq(10L), any(Set.class), any(Set.class));
        doAnswer(invocation -> {
            SysRole role = invocation.getArgument(0);
            role.setId(200L);
            return 1;
        }).when(sysRoleMapper).insert(any(SysRole.class));

        RoleCreateRequest request = new RoleCreateRequest();
        request.setRoleCode("REPAIR_WORKER");
        request.setRoleName("维修员");
        request.setParentRoleId(100L);
        request.setAllowCreateChild(0);
        request.setStatus(1);
        request.setSort(10);
        request.setPermissionIds(Set.of(1L));
        request.setScopeConfigs(Set.of(scope("SELF", null)));

        roleService.create(request);

        verify(sysRolePermissionMapper).batchInsert(200L, Set.of(1L));
        verify(sysRoleScopeMapper).batchInsert(any(Set.class));
    }

    @Test
    void shouldRejectCreateWithoutParentForChildOnlyCreator() {
        setPrincipal(10L, Set.of("repair:assign"), false);

        RoleCreateRequest request = new RoleCreateRequest();
        request.setRoleCode("CHILD_ROLE_NO_PARENT");
        request.setRoleName("子角色");
        request.setParentRoleId(null);
        request.setAllowCreateChild(0);
        request.setStatus(1);
        request.setSort(0);
        request.setPermissionIds(Set.of(1L));
        request.setScopeConfigs(Set.of(scope("SELF", null)));

        assertThrows(BizException.class, () -> roleService.create(request));
    }

    @Test
    void shouldRejectCreateWhenParentRoleNotOwned() {
        setPrincipal(10L, Set.of("repair:assign"), false);

        SysRole parentRole = new SysRole();
        parentRole.setId(100L);
        parentRole.setStatus(1);
        parentRole.setAllowCreateChild(1);
        when(sysRoleMapper.selectById(100L)).thenReturn(parentRole);
        when(sysRoleMapper.selectRoleIdsByUserId(10L)).thenReturn(List.of(999L));

        RoleCreateRequest request = new RoleCreateRequest();
        request.setRoleCode("OUTSIDE_PARENT");
        request.setRoleName("越权子角色");
        request.setParentRoleId(100L);
        request.setAllowCreateChild(0);
        request.setStatus(1);
        request.setSort(0);
        request.setPermissionIds(Set.of(1L));
        request.setScopeConfigs(Set.of(scope("SELF", null)));

        assertThrows(BizException.class, () -> roleService.create(request));
    }

    private RoleScopeConfigItem scope(String type, Long refId) {
        RoleScopeConfigItem item = new RoleScopeConfigItem();
        item.setScopeType(type);
        item.setScopeRefId(refId);
        return item;
    }

    private void setPrincipal(Long userId, Set<String> permissionCodes, boolean superAdmin) {
        LoginPrincipal principal = LoginPrincipal.builder()
            .userId(userId)
            .username("tester")
            .nickname("tester")
            .orgId(1L)
            .permissionCodes(permissionCodes)
            .roleCodes(Set.of("PROPERTY_ADMIN"))
            .superAdmin(superAdmin)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
    }
}
