package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import xxqqyyy.community.modules.activity.mapper.BizActivityMapper;
import xxqqyyy.community.modules.notice.mapper.BizNoticeMapper;
import xxqqyyy.community.modules.repair.mapper.BizRepairOrderMapper;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
import xxqqyyy.community.modules.system.model.DataScopeResult;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.modules.system.service.impl.DashboardServiceImpl;
import xxqqyyy.community.modules.system.vo.DashboardOverviewVO;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 看板服务测试。
 *
 * @author codex
 * @since 1.0.0
 */
class DashboardServiceImplTest {

    private SysUserMapper sysUserMapper;
    private BizNoticeMapper bizNoticeMapper;
    private BizActivityMapper bizActivityMapper;
    private BizRepairOrderMapper bizRepairOrderMapper;
    private DataScopeService dataScopeService;
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        sysUserMapper = mock(SysUserMapper.class);
        bizNoticeMapper = mock(BizNoticeMapper.class);
        bizActivityMapper = mock(BizActivityMapper.class);
        bizRepairOrderMapper = mock(BizRepairOrderMapper.class);
        dataScopeService = mock(DataScopeService.class);
        dashboardService = new DashboardServiceImpl(
            sysUserMapper,
            bizNoticeMapper,
            bizActivityMapper,
            bizRepairOrderMapper,
            dataScopeService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveByHighestPriorityRole() {
        mockPrincipal(
            1L,
            Set.of("COMMUNITY_ADMIN", "PROPERTY_ADMIN"),
            Set.of("dashboard:view", "sys:user:list", "repair:manage:list")
        );
        when(dataScopeService.resolveByUserId(1L)).thenReturn(DataScopeResult.builder().allAccess(true).build());
        when(sysUserMapper.countByScope(true, Set.of())).thenReturn(88L);
        when(bizNoticeMapper.countPublished(true, Set.of())).thenReturn(11L);
        when(bizActivityMapper.countPublished(true, Set.of())).thenReturn(9L);
        when(bizRepairOrderMapper.countByStatusInScope(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.eq(true), org.mockito.ArgumentMatchers.anySet()))
            .thenReturn(7L);

        DashboardOverviewVO result = dashboardService.overview();

        assertNotNull(result);
        assertEquals("community_admin_dashboard", result.getDashboardCode());
        assertEquals("/dashboard/community-admin", result.getDefaultHomeRoute());
        assertEquals(4, result.getCards().size());
        verify(dataScopeService).resolveByUserId(1L);
    }

    @Test
    void shouldResolveMaintainerByPermissionSignature() {
        mockPrincipal(
            9L,
            Set.of(),
            Set.of("dashboard:view", "repair:take", "repair:process")
        );
        when(bizRepairOrderMapper.countByMaintainerAndStatus(org.mockito.ArgumentMatchers.eq(9L), org.mockito.ArgumentMatchers.anyList()))
            .thenReturn(3L)
            .thenReturn(2L)
            .thenReturn(1L)
            .thenReturn(0L);

        DashboardOverviewVO result = dashboardService.overview();

        assertNotNull(result);
        assertEquals("maintainer_dashboard", result.getDashboardCode());
        assertEquals("/dashboard/maintainer", result.getDefaultHomeRoute());
        assertEquals(4, result.getCards().size());
    }

    private void mockPrincipal(Long userId, Set<String> roleCodes, Set<String> permissionCodes) {
        LoginPrincipal principal = LoginPrincipal.builder()
            .userId(userId)
            .username("dashboard-user")
            .orgId(1L)
            .superAdmin(false)
            .roleCodes(roleCodes)
            .permissionCodes(permissionCodes)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.toAuthorities())
        );
    }
}
