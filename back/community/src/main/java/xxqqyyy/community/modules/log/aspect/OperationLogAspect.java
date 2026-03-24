package xxqqyyy.community.modules.log.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xxqqyyy.community.common.util.TraceIdUtil;
import xxqqyyy.community.modules.log.annotation.OperationLog;
import xxqqyyy.community.modules.log.entity.LogOperation;
import xxqqyyy.community.modules.log.service.OperationLogService;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 操作日志切面，记录关键接口调用摘要。
 *
 * @author codex
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final ObjectMapper objectMapper;
    private final OperationLogService operationLogService;

    @Around("@annotation(xxqqyyy.community.modules.log.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationLog annotation = signature.getMethod().getAnnotation(OperationLog.class);
        HttpServletRequest request = getRequest();
        LogOperation operation = new LogOperation();
        operation.setOperationModule(annotation.module());
        operation.setOperationType(annotation.type());
        operation.setRequestUri(request == null ? null : request.getRequestURI());
        operation.setRequestMethod(request == null ? null : request.getMethod());
        operation.setRequestBody(writeValue(joinPoint.getArgs()));
        operation.setTraceId(TraceIdUtil.getTraceId());
        operation.setOperationTime(LocalDateTime.now());
        fillOperator(operation);
        try {
            Object result = joinPoint.proceed();
            operation.setSuccessFlag(1);
            operation.setResponseBody(writeValue(result));
            operationLogService.record(operation);
            return result;
        } catch (Throwable ex) {
            operation.setSuccessFlag(0);
            operation.setErrorMessage(ex.getMessage());
            operationLogService.record(operation);
            throw ex;
        }
    }

    private void fillOperator(LogOperation operation) {
        try {
            LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
            operation.setUserId(principal.getUserId());
            operation.setUsername(principal.getUsername());
        } catch (Exception ex) {
            log.debug("记录操作日志时未检测到登录用户");
        }
    }

    private String writeValue(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "[serialize-failed]";
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }
}

