package xxqqyyy.community.common.util;

import java.util.UUID;
import org.slf4j.MDC;

/**
 * TraceId 工具类，用于统一链路追踪字段。
 *
 * @author codex
 * @since 1.0.0
 */
public final class TraceIdUtil {

    public static final String TRACE_ID_KEY = "traceId";

    private TraceIdUtil() {
    }

    /**
     * 获取当前 traceId，没有则自动创建。
     *
     * @return traceId
     */
    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
            MDC.put(TRACE_ID_KEY, traceId);
        }
        return traceId;
    }
}

