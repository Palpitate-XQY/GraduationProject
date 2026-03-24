package xxqqyyy.community.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一接口返回体。
 *
 * @param <T> 数据泛型
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应体")
public class ApiResponse<T> {

    /**
     * 业务状态码。
     */
    @Schema(description = "业务状态码")
    private int code;

    /**
     * 业务消息。
     */
    @Schema(description = "响应消息")
    private String message;

    /**
     * 业务数据。
     */
    @Schema(description = "响应数据")
    private T data;

    /**
     * 请求链路追踪ID。
     */
    @Schema(description = "链路追踪ID")
    private String traceId;

    /**
     * 返回成功响应。
     *
     * @param data 返回数据
     * @param <T> 泛型
     * @return 响应体
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .code(0)
            .message("success")
            .data(data)
            .traceId(xxqqyyy.community.common.util.TraceIdUtil.getTraceId())
            .build();
    }

    /**
     * 返回成功响应（无数据）。
     *
     * @return 响应体
     */
    public static ApiResponse<Void> success() {
        return success(null);
    }

    /**
     * 返回失败响应。
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 响应体
     */
    public static ApiResponse<Void> fail(int code, String message) {
        return ApiResponse.<Void>builder()
            .code(code)
            .message(message)
            .traceId(xxqqyyy.community.common.util.TraceIdUtil.getTraceId())
            .build();
    }
}

