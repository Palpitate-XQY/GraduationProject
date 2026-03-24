package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import xxqqyyy.community.modules.org.entity.SysOrg;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.resident.dto.ResidentProfileAdminUpdateRequest;
import xxqqyyy.community.modules.resident.dto.ResidentProfileMyUpdateRequest;
import xxqqyyy.community.modules.resident.entity.BizResidentProfile;
import xxqqyyy.community.modules.resident.mapper.BizResidentProfileMapper;
import xxqqyyy.community.modules.resident.service.impl.ResidentProfileServiceImpl;
import xxqqyyy.community.modules.resident.vo.ResidentProfileVO;
import xxqqyyy.community.modules.system.entity.SysUser;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 居民档案服务测试。
 *
 * @author codex
 * @since 1.0.0
 */
class ResidentProfileServiceImplTest {

    private BizResidentProfileMapper bizResidentProfileMapper;
    private SysUserMapper sysUserMapper;
    private SysOrgMapper sysOrgMapper;
    private DataScopeService dataScopeService;
    private ResidentProfileServiceImpl residentProfileService;

    @BeforeEach
    void setUp() {
        bizResidentProfileMapper = mock(BizResidentProfileMapper.class);
        sysUserMapper = mock(SysUserMapper.class);
        sysOrgMapper = mock(SysOrgMapper.class);
        dataScopeService = mock(DataScopeService.class);
        residentProfileService = new ResidentProfileServiceImpl(
            bizResidentProfileMapper,
            sysUserMapper,
            sysOrgMapper,
            dataScopeService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateProfileWhenResidentUpdatesSelfAndProfileNotExists() {
        mockPrincipal(2L, Set.of("resident:profile:my:update"));
        when(sysUserMapper.selectById(2L)).thenReturn(buildUser(2L, 3L));
        when(sysOrgMapper.selectById(3L)).thenReturn(buildComplex(3L, 2L, "幸福家园小区"));
        when(sysOrgMapper.selectById(2L)).thenReturn(buildCommunity(2L, "和谐社区"));
        when(bizResidentProfileMapper.selectByUserId(2L)).thenReturn(null);

        ResidentProfileMyUpdateRequest request = new ResidentProfileMyUpdateRequest();
        request.setRoomNo("1栋101");
        request.setEmergencyContact("张三");
        request.setEmergencyPhone("13900000001");
        residentProfileService.updateMyProfile(request);

        verify(bizResidentProfileMapper, times(1)).insert(any(BizResidentProfile.class));
    }

    @Test
    void shouldUpsertAndSyncUserOrgWhenAdminUpdatesResidentProfile() {
        mockPrincipal(1L, Set.of("resident:profile:update"));
        when(sysUserMapper.selectById(10L)).thenReturn(buildUser(10L, 3L));
        when(sysOrgMapper.selectById(5L)).thenReturn(buildComplex(5L, 2L, "新苑小区"));
        when(sysOrgMapper.selectById(2L)).thenReturn(buildCommunity(2L, "和谐社区"));
        when(bizResidentProfileMapper.selectByUserId(10L)).thenReturn(buildProfile(100L, 10L, 2L, 3L));
        when(sysUserMapper.updateOrgId(10L, 5L, 1L)).thenReturn(1);

        ResidentProfileAdminUpdateRequest request = new ResidentProfileAdminUpdateRequest();
        request.setUserId(10L);
        request.setComplexOrgId(5L);
        request.setRoomNo("2栋201");
        request.setEmergencyContact("李四");
        request.setEmergencyPhone("13800000002");
        residentProfileService.upsertByAdmin(request);

        verify(dataScopeService).assertOrgAccessible(1L, 3L);
        verify(dataScopeService).assertOrgAccessible(1L, 5L);
        verify(bizResidentProfileMapper).updateByUserId(any(BizResidentProfile.class));
        verify(sysUserMapper).updateOrgId(10L, 5L, 1L);
    }

    @Test
    void shouldGetProfileDetail() {
        mockPrincipal(1L, Set.of("resident:profile:view"));
        when(sysUserMapper.selectById(10L)).thenReturn(buildUser(10L, 3L));
        when(bizResidentProfileMapper.selectByUserId(10L)).thenReturn(buildProfile(100L, 10L, 2L, 3L));
        when(sysOrgMapper.selectById(2L)).thenReturn(buildCommunity(2L, "和谐社区"));
        when(sysOrgMapper.selectById(3L)).thenReturn(buildComplex(3L, 2L, "幸福家园小区"));

        ResidentProfileVO result = residentProfileService.getByUserId(10L);

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
        assertEquals("和谐社区", result.getCommunityOrgName());
        assertEquals("幸福家园小区", result.getComplexOrgName());
        verify(dataScopeService).assertOrgAccessible(1L, 3L);
    }

    private void mockPrincipal(Long userId, Set<String> permissions) {
        LoginPrincipal principal = LoginPrincipal.builder()
            .userId(userId)
            .username("tester")
            .orgId(1L)
            .superAdmin(false)
            .permissionCodes(permissions)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.toAuthorities())
        );
    }

    private SysUser buildUser(Long id, Long orgId) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setOrgId(orgId);
        return user;
    }

    private SysOrg buildCommunity(Long id, String name) {
        SysOrg org = new SysOrg();
        org.setId(id);
        org.setOrgType("COMMUNITY");
        org.setStatus(1);
        org.setOrgName(name);
        return org;
    }

    private SysOrg buildComplex(Long id, Long parentId, String name) {
        SysOrg org = new SysOrg();
        org.setId(id);
        org.setParentId(parentId);
        org.setOrgType("COMPLEX");
        org.setStatus(1);
        org.setOrgName(name);
        return org;
    }

    private BizResidentProfile buildProfile(Long id, Long userId, Long communityOrgId, Long complexOrgId) {
        BizResidentProfile profile = new BizResidentProfile();
        profile.setId(id);
        profile.setUserId(userId);
        profile.setCommunityOrgId(communityOrgId);
        profile.setComplexOrgId(complexOrgId);
        profile.setRoomNo("1栋101");
        profile.setEmergencyContact("王五");
        profile.setEmergencyPhone("13900000003");
        return profile;
    }
}
