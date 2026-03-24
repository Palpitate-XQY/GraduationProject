package xxqqyyy.community.common.enums;

import lombok.Getter;

/**
 * 统一错误码定义。
 *
 * @author codex
 * @since 1.0.0
 */
@Getter
public enum ErrorCode {
    SUCCESS(0, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "系统内部异常"),
    BIZ_ERROR(1000, "业务异常"),
    LOGIN_FAILED(1001, "用户名或密码错误"),
    USER_DISABLED(1002, "用户已禁用"),
    TOKEN_INVALID(1003, "令牌无效"),
    DATA_SCOPE_DENIED(1004, "数据范围越权"),
    ILLEGAL_STATE_FLOW(1005, "非法状态流转");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

