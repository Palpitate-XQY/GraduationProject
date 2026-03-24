package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
    private DataScopeServiceImpl dataScopeService;

    @BeforeEach
    void setUp() {
        sysRoleMapper = Mockito.mock(SysRoleMapper.class);
        sysRoleScopeMapper = Mockito.mock(SysRoleScopeMapper.class);
        sysOrgMapper = Mockito.mock(SysOrgMapper.class);
        dataScopeService = new DataScopeServiceImpl(sysRoleMapper, sysRoleScopeMapper, sysOrgMapper);
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
}

