package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import xxqqyyy.community.modules.org.entity.BizComplexPropertyRel;
import xxqqyyy.community.modules.org.mapper.BizComplexPropertyRelMapper;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.system.entity.SysRoleScope;
import xxqqyyy.community.modules.system.mapper.SysRoleMapper;
import xxqqyyy.community.modules.system.mapper.SysRoleScopeMapper;
import xxqqyyy.community.modules.system.model.DataScopeResult;
import xxqqyyy.community.modules.system.service.impl.DataScopeServiceImpl;

/**
 * 数据范围服务单元测试。
 *
 * @author codex
 * @since 1.0.0
 */
class DataScopeServiceTest {

    private SysRoleMapper sysRoleMapper;
    private SysRoleScopeMapper sysRoleScopeMapper;
    private SysOrgMapper sysOrgMapper;
    private BizComplexPropertyRelMapper bizComplexPropertyRelMapper;
    private DataScopeServiceImpl dataScopeService;

    @BeforeEach
    void setUp() {
        sysRoleMapper = Mockito.mock(SysRoleMapper.class);
        sysRoleScopeMapper = Mockito.mock(SysRoleScopeMapper.class);
        sysOrgMapper = Mockito.mock(SysOrgMapper.class);
        bizComplexPropertyRelMapper = Mockito.mock(BizComplexPropertyRelMapper.class);
        dataScopeService = new DataScopeServiceImpl(sysRoleMapper, sysRoleScopeMapper, sysOrgMapper, bizComplexPropertyRelMapper);
    }

    @Test
    void shouldReturnAllAccessWhenRoleContainsAllScope() {
        when(sysRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("STREET_ADMIN"));
        when(sysRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(2L));
        SysRoleScope scope = new SysRoleScope();
        scope.setRoleId(2L);
        scope.setScopeType("ALL");
        scope.setScopeRefId(null);
        when(sysRoleScopeMapper.selectByRoleIds(List.of(2L))).thenReturn(List.of(scope));
        DataScopeResult result = dataScopeService.resolveByUserId(1L);
        assertTrue(result.isAllAccess());
    }

    @Test
    void shouldExpandComplexWhenRoleScopeIsPropertyCompany() {
        when(sysRoleMapper.selectRoleCodesByUserId(2L)).thenReturn(List.of("PROPERTY_ADMIN"));
        when(sysRoleMapper.selectRoleIdsByUserId(2L)).thenReturn(List.of(3L));

        SysRoleScope scope = new SysRoleScope();
        scope.setRoleId(3L);
        scope.setScopeType("PROPERTY_COMPANY");
        scope.setScopeRefId(300L);
        when(sysRoleScopeMapper.selectByRoleIds(List.of(3L))).thenReturn(List.of(scope));

        BizComplexPropertyRel rel = new BizComplexPropertyRel();
        rel.setPropertyCompanyOrgId(300L);
        rel.setComplexOrgId(500L);
        rel.setStatus(1);
        when(bizComplexPropertyRelMapper.selectByPropertyCompanyOrgId(300L)).thenReturn(List.of(rel));

        DataScopeResult result = dataScopeService.resolveByUserId(2L);
        assertTrue(result.getSafeOrgIds().containsAll(Set.of(300L, 500L)));
    }
}
