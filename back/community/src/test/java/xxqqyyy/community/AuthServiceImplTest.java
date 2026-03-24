package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.auth.dto.LoginRequest;
import xxqqyyy.community.modules.auth.dto.ResidentRegisterRequest;
import xxqqyyy.community.modules.auth.dto.ResetPasswordByCodeRequest;
import xxqqyyy.community.modules.auth.dto.SendResetCodeRequest;
import xxqqyyy.community.modules.auth.service.impl.AuthServiceImpl;
import xxqqyyy.community.modules.auth.vo.LoginVO;
import xxqqyyy.community.modules.auth.vo.SendResetCodeVO;
import xxqqyyy.community.modules.log.service.LoginLogService;
import xxqqyyy.community.modules.org.entity.SysOrg;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.resident.mapper.BizResidentProfileMapper;
import xxqqyyy.community.modules.system.entity.SysRole;
import xxqqyyy.community.modules.system.entity.SysUser;
import xxqqyyy.community.modules.system.mapper.SysRoleMapper;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
import xxqqyyy.community.modules.system.mapper.SysUserRoleMapper;
import xxqqyyy.community.security.JwtProperties;
import xxqqyyy.community.security.service.JwtTokenService;
import xxqqyyy.community.security.service.TokenBlacklistService;

/**
 * 认证服务单元测试。
 *
 * @author codex
 * @since 1.0.0
 */
class AuthServiceImplTest {

    private SysUserMapper sysUserMapper;
    private SysRoleMapper sysRoleMapper;
    private SysUserRoleMapper sysUserRoleMapper;
    private BizResidentProfileMapper bizResidentProfileMapper;
    private SysOrgMapper sysOrgMapper;
    private JwtTokenService jwtTokenService;
    private TokenBlacklistService tokenBlacklistService;
    private LoginLogService loginLogService;
    private StringRedisTemplate stringRedisTemplate;
    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> valueOperations;
    private PasswordEncoder passwordEncoder;
    private JwtProperties jwtProperties;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        sysUserMapper = Mockito.mock(SysUserMapper.class);
        sysRoleMapper = Mockito.mock(SysRoleMapper.class);
        sysUserRoleMapper = Mockito.mock(SysUserRoleMapper.class);
        bizResidentProfileMapper = Mockito.mock(BizResidentProfileMapper.class);
        sysOrgMapper = Mockito.mock(SysOrgMapper.class);
        jwtTokenService = Mockito.mock(JwtTokenService.class);
        tokenBlacklistService = Mockito.mock(TokenBlacklistService.class);
        loginLogService = Mockito.mock(LoginLogService.class);
        stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        valueOperations = Mockito.mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtProperties = new JwtProperties();
        jwtProperties.setExpireMinutes(120);
        authService = new AuthServiceImpl(
            sysUserMapper,
            sysRoleMapper,
            sysUserRoleMapper,
            bizResidentProfileMapper,
            sysOrgMapper,
            jwtTokenService,
            tokenBlacklistService,
            loginLogService,
            stringRedisTemplate,
            passwordEncoder,
            jwtProperties
        );
    }

    @Test
    void shouldLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123456");
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setStatus(1);
        user.setMustChangePassword(1);
        when(sysUserMapper.selectByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("Admin@123456", "encoded")).thenReturn(true);
        when(jwtTokenService.generateToken(1L, "admin")).thenReturn("token");
        LoginVO vo = authService.login(request, "127.0.0.1", "junit");
        assertEquals("token", vo.getAccessToken());
        assertEquals(7200, vo.getExpiresIn());
    }

    @Test
    void shouldThrowWhenPasswordWrong() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setStatus(1);
        when(sysUserMapper.selectByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
        assertThrows(BizException.class, () -> authService.login(request, "127.0.0.1", "junit"));
    }

    @Test
    void shouldRegisterResidentSuccess() {
        ResidentRegisterRequest request = new ResidentRegisterRequest();
        request.setUsername("resident_new");
        request.setPassword("Resident@123");
        request.setNickname("新居民");
        request.setPhone("13900000009");
        request.setComplexOrgId(3L);
        request.setRoomNo("2栋1单元202");

        SysRole residentRole = new SysRole();
        residentRole.setId(5L);
        residentRole.setRoleCode("RESIDENT");
        when(sysRoleMapper.selectByCode("RESIDENT")).thenReturn(residentRole);

        SysOrg complex = new SysOrg();
        complex.setId(3L);
        complex.setParentId(2L);
        complex.setOrgType("COMPLEX");
        complex.setStatus(1);
        when(sysOrgMapper.selectById(3L)).thenReturn(complex);

        SysOrg community = new SysOrg();
        community.setId(2L);
        community.setOrgType("COMMUNITY");
        when(sysOrgMapper.selectById(2L)).thenReturn(community);

        when(sysUserMapper.countByUsername("resident_new", null)).thenReturn(0L);
        when(passwordEncoder.encode("Resident@123")).thenReturn("encoded-resident");
        doAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(88L);
            return 1;
        }).when(sysUserMapper).insert(any(SysUser.class));

        authService.registerResident(request);

        verify(sysUserMapper).insert(any(SysUser.class));
        verify(sysUserRoleMapper).batchInsert(eq(88L), eq(Set.of(5L)));
        verify(bizResidentProfileMapper).insert(any());
    }

    @Test
    void shouldSendResetCodeAndResetPassword() {
        SysUser user = new SysUser();
        user.setId(2L);
        user.setUsername("resident_demo");
        user.setPhone("13900000000");
        user.setStatus(1);
        when(sysUserMapper.selectByUsername("resident_demo")).thenReturn(user);

        SendResetCodeRequest sendRequest = new SendResetCodeRequest();
        sendRequest.setUsername("resident_demo");
        sendRequest.setPhone("13900000000");
        SendResetCodeVO response = authService.sendResetCode(sendRequest);
        assertNotNull(response.getDebugCode());
        assertEquals(6, response.getDebugCode().length());

        when(valueOperations.get(anyString())).thenReturn("123456");
        when(passwordEncoder.encode("NewPass@123")).thenReturn("encoded-new");
        ResetPasswordByCodeRequest resetRequest = new ResetPasswordByCodeRequest();
        resetRequest.setUsername("resident_demo");
        resetRequest.setPhone("13900000000");
        resetRequest.setCode("123456");
        resetRequest.setNewPassword("NewPass@123");
        authService.resetPasswordByCode(resetRequest);

        verify(sysUserMapper).updatePassword(eq(2L), eq("encoded-new"), eq(0), anyLong());
    }
}
