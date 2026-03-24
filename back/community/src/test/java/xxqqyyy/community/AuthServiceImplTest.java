package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.auth.dto.LoginRequest;
import xxqqyyy.community.modules.auth.service.impl.AuthServiceImpl;
import xxqqyyy.community.modules.auth.vo.LoginVO;
import xxqqyyy.community.modules.log.service.LoginLogService;
import xxqqyyy.community.modules.system.entity.SysUser;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
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
    private JwtTokenService jwtTokenService;
    private TokenBlacklistService tokenBlacklistService;
    private LoginLogService loginLogService;
    private PasswordEncoder passwordEncoder;
    private JwtProperties jwtProperties;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        sysUserMapper = Mockito.mock(SysUserMapper.class);
        jwtTokenService = Mockito.mock(JwtTokenService.class);
        tokenBlacklistService = Mockito.mock(TokenBlacklistService.class);
        loginLogService = Mockito.mock(LoginLogService.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtProperties = new JwtProperties();
        jwtProperties.setExpireMinutes(120);
        authService = new AuthServiceImpl(
            sysUserMapper,
            jwtTokenService,
            tokenBlacklistService,
            loginLogService,
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
}

