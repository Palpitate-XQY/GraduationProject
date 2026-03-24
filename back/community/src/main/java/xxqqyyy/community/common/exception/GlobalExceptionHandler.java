package xxqqyyy.community.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.common.enums.ErrorCode;

/**
 * 全局异常处理器，保证错误响应结构统一。
 *
 * @author codex
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException ex, HttpServletRequest request) {
        log.warn("业务异常: path={}, msg={}", request.getRequestURI(), ex.getMessage());
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        BindException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class
    })
    public ApiResponse<Void> handleValidationException(Exception ex, HttpServletRequest request) {
        log.warn("参数异常: path={}, msg={}", request.getRequestURI(), ex.getMessage());
        return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ApiResponse<Void> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("认证异常: path={}, msg={}", request.getRequestURI(), ex.getMessage());
        return ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("权限异常: path={}, msg={}", request.getRequestURI(), ex.getMessage());
        return ApiResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOtherException(Exception ex, HttpServletRequest request) {
        log.error("系统异常: path={}", request.getRequestURI(), ex);
        return ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }
}

