package xxqqyyy.community.modules.auth.service;

import xxqqyyy.community.modules.auth.dto.LoginRequest;
import xxqqyyy.community.modules.auth.dto.ResidentRegisterRequest;
import xxqqyyy.community.modules.auth.dto.ResetPasswordByCodeRequest;
import xxqqyyy.community.modules.auth.dto.SendResetCodeRequest;
import xxqqyyy.community.modules.auth.dto.UpdatePasswordRequest;
import xxqqyyy.community.modules.auth.vo.CurrentUserVO;
import xxqqyyy.community.modules.auth.vo.SendResetCodeVO;
import xxqqyyy.community.modules.auth.vo.LoginVO;

/**
 * 认证服务接口。
 *
 * @author codex
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 用户登录。
     *
     * @param request   登录请求
     * @param ip        请求IP
     * @param userAgent 请求客户端信息
     * @return 登录响应
     */
    LoginVO login(LoginRequest request, String ip, String userAgent);

    /**
     * 当前登录用户信息。
     *
     * @return 当前用户
     */
    CurrentUserVO me();

    /**
     * 用户登出。
     *
     * @param token 当前访问令牌
     */
    void logout(String token);

    /**
     * 修改密码。
     *
     * @param request 密码修改请求
     */
    void updatePassword(UpdatePasswordRequest request);

    /**
     * 居民自助注册。
     *
     * @param request 注册请求
     */
    void registerResident(ResidentRegisterRequest request);

    /**
     * 发送忘记密码验证码。
     *
     * @param request 请求参数
     * @return 验证码信息（一期联调用）
     */
    SendResetCodeVO sendResetCode(SendResetCodeRequest request);

    /**
     * 使用验证码重置密码。
     *
     * @param request 重置请求
     */
    void resetPasswordByCode(ResetPasswordByCodeRequest request);
}
