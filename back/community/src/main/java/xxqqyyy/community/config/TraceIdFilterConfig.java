package xxqqyyy.community.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import xxqqyyy.community.common.util.TraceIdUtil;

/**
 * TraceId 过滤器配置。
 *
 * @author codex
 * @since 1.0.0
 */
@Configuration
public class TraceIdFilterConfig {

    /**
     * 注册 TraceId 过滤器。
     *
     * @return 过滤器对象
     */
    @Bean
    public OncePerRequestFilter traceIdFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
                try {
                    String traceId = request.getHeader("X-Trace-Id");
                    if (traceId == null || traceId.isBlank()) {
                        traceId = TraceIdUtil.getTraceId();
                    } else {
                        MDC.put(TraceIdUtil.TRACE_ID_KEY, traceId);
                    }
                    response.setHeader("X-Trace-Id", traceId);
                    filterChain.doFilter(request, response);
                } finally {
                    MDC.remove(TraceIdUtil.TRACE_ID_KEY);
                }
            }
        };
    }
}

