package xxqqyyy.community.modules.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.modules.auth.dto.LoginRequest;
import xxqqyyy.community.modules.auth.dto.ResidentRegisterRequest;
import xxqqyyy.community.modules.auth.dto.ResetPasswordByCodeRequest;
import xxqqyyy.community.modules.auth.dto.SendResetCodeRequest;
import xxqqyyy.community.modules.auth.dto.UpdatePasswordRequest;
import xxqqyyy.community.modules.auth.service.AuthService;
import xxqqyyy.community.modules.auth.vo.CurrentUserVO;
import xxqqyyy.community.modules.auth.vo.LoginVO;
import xxqqyyy.community.modules.auth.vo.SendResetCodeVO;
import xxqqyyy.community.modules.log.annotation.OperationLog;

/**
 * 认证相关接口。
 *
 * @author codex
 * @since 1.0.0
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录接口。
     *
     * @param request     登录请求
     * @param servletRequest http 请求
     * @return 登录结果
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @OperationLog(module = "认证", type = "登录")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String ip = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader(HttpHeaders.USER_AGENT);
        return ApiResponse.success(authService.login(request, ip, userAgent));
    }

    /**
     * 居民自助注册。
     *
     * @param request 注册请求
     * @return 统一响应
     */
    @Operation(summary = "居民注册")
    @PostMapping("/resident/register")
    @OperationLog(module = "认证", type = "居民注册")
    public ApiResponse<Void> registerResident(@Valid @RequestBody ResidentRegisterRequest request) {
        authService.registerResident(request);
        return ApiResponse.success();
    }

    /**
     * 发送忘记密码验证码。
     *
     * @param request 请求参数
     * @return 验证码信息
     */
    @Operation(summary = "发送忘记密码验证码")
    @PostMapping("/password/reset-code")
    @OperationLog(module = "认证", type = "发送重置验证码")
    public ApiResponse<SendResetCodeVO> sendResetCode(@Valid @RequestBody SendResetCodeRequest request) {
        return ApiResponse.success(authService.sendResetCode(request));
    }

    /**
     * 使用验证码重置密码。
     *
     * @param request 请求参数
     * @return 统一响应
     */
    @Operation(summary = "验证码重置密码")
    @PostMapping("/password/reset-by-code")
    @OperationLog(module = "认证", type = "验证码重置密码")
    public ApiResponse<Void> resetPasswordByCode(@Valid @RequestBody ResetPasswordByCodeRequest request) {
        authService.resetPasswordByCode(request);
        return ApiResponse.success();
    }

    /**
     * 查询当前登录用户信息。
     *
     * @return 当前用户信息
     */
    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public ApiResponse<CurrentUserVO> me() {
        return ApiResponse.success(authService.me());
    }

    /**
     * 用户登出接口。
     *
     * @param authorization Bearer Token
     * @return 统一响应
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    @OperationLog(module = "认证", type = "登出")
    public ApiResponse<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authService.logout(authorization.substring(7));
        }
        return ApiResponse.success();
    }

    /**
     * 修改密码接口。
     *
     * @param request 请求体
     * @return 统一响应
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    @OperationLog(module = "认证", type = "修改密码")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(request);
        return ApiResponse.success();
    }
}
