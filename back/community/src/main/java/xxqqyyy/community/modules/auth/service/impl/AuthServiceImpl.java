package xxqqyyy.community.modules.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.auth.dto.LoginRequest;
import xxqqyyy.community.modules.auth.dto.UpdatePasswordRequest;
import xxqqyyy.community.modules.auth.service.AuthService;
import xxqqyyy.community.modules.auth.vo.CurrentUserVO;
import xxqqyyy.community.modules.auth.vo.LoginVO;
import xxqqyyy.community.modules.log.service.LoginLogService;
import xxqqyyy.community.modules.system.entity.SysUser;
import xxqqyyy.community.modules.system.enums.UserStatusEnum;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
import xxqqyyy.community.security.JwtProperties;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;
import xxqqyyy.community.security.service.JwtTokenService;
import xxqqyyy.community.security.service.TokenBlacklistService;

/**
 * 认证服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final JwtTokenService jwtTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginLogService loginLogService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    @Override
    public LoginVO login(LoginRequest request, String ip, String userAgent) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginLogService.record(null, request.getUsername(), false, "用户名或密码错误", ip, userAgent);
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }
        if (user.getStatus() == null || user.getStatus() != UserStatusEnum.ENABLED.getCode()) {
            loginLogService.record(user.getId(), user.getUsername(), false, "用户已禁用", ip, userAgent);
            throw new BizException(ErrorCode.USER_DISABLED);
        }
        String token = jwtTokenService.generateToken(user.getId(), user.getUsername());
        loginLogService.record(user.getId(), user.getUsername(), true, "登录成功", ip, userAgent);
        return LoginVO.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(jwtProperties.getExpireMinutes() * 60)
            .mustChangePassword(user.getMustChangePassword())
            .build();
    }

    @Override
    public CurrentUserVO me() {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        return CurrentUserVO.builder()
            .userId(principal.getUserId())
            .username(principal.getUsername())
            .nickname(principal.getNickname())
            .orgId(principal.getOrgId())
            .roleCodes(principal.getRoleCodes())
            .permissionCodes(principal.getPermissionCodes())
            .build();
    }

    @Override
    public void logout(String token) {
        try {
            Jws<Claims> claimsJws = jwtTokenService.parse(token);
            Claims claims = claimsJws.getPayload();
            tokenBlacklistService.blacklist(claims.getId(), jwtTokenService.remainSeconds(claims));
        } catch (Exception ignored) {
            // 无论 token 是否可解析都返回成功，避免暴露鉴权细节。
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysUser user = sysUserMapper.selectById(principal.getUserId());
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "旧密码不正确");
        }
        String newPassword = passwordEncoder.encode(request.getNewPassword());
        sysUserMapper.updatePassword(user.getId(), newPassword, 0, principal.getUserId());
    }
}

