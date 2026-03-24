package xxqqyyy.community.modules.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解，标记关键业务接口。
 *
 * @author codex
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /**
     * 模块名称。
     */
    String module();

    /**
     * 操作类型。
     */
    String type();
}

