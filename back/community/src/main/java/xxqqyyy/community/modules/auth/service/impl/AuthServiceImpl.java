package xxqqyyy.community.modules.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.auth.dto.LoginRequest;
import xxqqyyy.community.modules.auth.dto.ResidentRegisterRequest;
import xxqqyyy.community.modules.auth.dto.ResetPasswordByCodeRequest;
import xxqqyyy.community.modules.auth.dto.SendResetCodeRequest;
import xxqqyyy.community.modules.auth.dto.UpdatePasswordRequest;
import xxqqyyy.community.modules.auth.service.AuthService;
import xxqqyyy.community.modules.auth.vo.CurrentUserVO;
import xxqqyyy.community.modules.auth.vo.LoginVO;
import xxqqyyy.community.modules.auth.vo.SendResetCodeVO;
import xxqqyyy.community.modules.log.service.LoginLogService;
import xxqqyyy.community.modules.org.entity.SysOrg;
import xxqqyyy.community.modules.org.enums.OrgTypeEnum;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.resident.entity.BizResidentProfile;
import xxqqyyy.community.modules.resident.mapper.BizResidentProfileMapper;
import xxqqyyy.community.modules.system.entity.SysRole;
import xxqqyyy.community.modules.system.entity.SysUser;
import xxqqyyy.community.modules.system.enums.UserStatusEnum;
import xxqqyyy.community.modules.system.mapper.SysRoleMapper;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
import xxqqyyy.community.modules.system.mapper.SysUserRoleMapper;
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

    private static final String RESIDENT_ROLE_CODE = "RESIDENT";
    private static final long RESET_CODE_EXPIRE_SECONDS = 600L;
    private static final String RESET_CODE_KEY_PREFIX = "community:auth:pwd-reset:";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final BizResidentProfileMapper bizResidentProfileMapper;
    private final SysOrgMapper sysOrgMapper;
    private final JwtTokenService jwtTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginLogService loginLogService;
    private final StringRedisTemplate stringRedisTemplate;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerResident(ResidentRegisterRequest request) {
        if (sysUserMapper.countByUsername(request.getUsername(), null) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "用户名已存在");
        }
        SysOrg complexOrg = requireEnabledComplexOrg(request.getComplexOrgId());
        SysRole residentRole = requireResidentRole();

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(UserStatusEnum.ENABLED.getCode());
        user.setOrgId(complexOrg.getId());
        user.setMustChangePassword(0);
        user.setCreateBy(0L);
        user.setUpdateBy(0L);
        sysUserMapper.insert(user);

        sysUserRoleMapper.batchInsert(user.getId(), Set.of(residentRole.getId()));

        BizResidentProfile profile = new BizResidentProfile();
        profile.setUserId(user.getId());
        profile.setCommunityOrgId(resolveCommunityOrgId(complexOrg));
        profile.setComplexOrgId(complexOrg.getId());
        profile.setRoomNo(request.getRoomNo());
        profile.setEmergencyContact(request.getEmergencyContact());
        profile.setEmergencyPhone(request.getEmergencyPhone());
        profile.setCreateBy(user.getId());
        profile.setUpdateBy(user.getId());
        bizResidentProfileMapper.insert(profile);
    }

    @Override
    public SendResetCodeVO sendResetCode(SendResetCodeRequest request) {
        SysUser user = requireUserAndPhoneMatched(request.getUsername(), request.getPhone());
        if (user.getStatus() == null || user.getStatus() != UserStatusEnum.ENABLED.getCode()) {
            throw new BizException(ErrorCode.USER_DISABLED);
        }
        String code = generateResetCode();
        stringRedisTemplate.opsForValue().set(resetCodeKey(request.getUsername()), code, Duration.ofSeconds(RESET_CODE_EXPIRE_SECONDS));
        return SendResetCodeVO.builder()
            .expireSeconds(RESET_CODE_EXPIRE_SECONDS)
            .debugCode(code)
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByCode(ResetPasswordByCodeRequest request) {
        SysUser user = requireUserAndPhoneMatched(request.getUsername(), request.getPhone());
        String key = resetCodeKey(request.getUsername());
        String cachedCode = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(cachedCode) || !cachedCode.equals(request.getCode())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "验证码错误或已过期");
        }
        String newPassword = passwordEncoder.encode(request.getNewPassword());
        sysUserMapper.updatePassword(user.getId(), newPassword, 0, user.getId());
        stringRedisTemplate.delete(key);
    }

    private SysRole requireResidentRole() {
        SysRole role = sysRoleMapper.selectByCode(RESIDENT_ROLE_CODE);
        if (role == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "居民角色未初始化，请先执行 SQL 初始化脚本");
        }
        return role;
    }

    private SysOrg requireEnabledComplexOrg(Long complexOrgId) {
        SysOrg org = sysOrgMapper.selectById(complexOrgId);
        if (org == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "所属小区不存在");
        }
        if (!OrgTypeEnum.COMPLEX.getCode().equalsIgnoreCase(org.getOrgType())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "所属组织必须为小区");
        }
        if (org.getStatus() == null || org.getStatus() != 1) {
            throw new BizException(ErrorCode.BIZ_ERROR, "所属小区已停用");
        }
        return org;
    }

    private Long resolveCommunityOrgId(SysOrg complexOrg) {
        if (complexOrg.getParentId() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "小区未配置所属社区");
        }
        SysOrg communityOrg = sysOrgMapper.selectById(complexOrg.getParentId());
        if (communityOrg == null || !OrgTypeEnum.COMMUNITY.getCode().equalsIgnoreCase(communityOrg.getOrgType())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "小区所属社区配置非法");
        }
        return communityOrg.getId();
    }

    private SysUser requireUserAndPhoneMatched(String username, String phone) {
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (StringUtils.isBlank(user.getPhone()) || !user.getPhone().equals(phone)) {
            throw new BizException(ErrorCode.BIZ_ERROR, "用户名与手机号不匹配");
        }
        return user;
    }

    private String generateResetCode() {
        int randomCode = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(randomCode);
    }

    private String resetCodeKey(String username) {
        return RESET_CODE_KEY_PREFIX + username;
    }
}
