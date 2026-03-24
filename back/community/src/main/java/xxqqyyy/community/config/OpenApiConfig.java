package xxqqyyy.community.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置。
 *
 * @author codex
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * 系统 OpenAPI 元信息。
     *
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI communityOpenApi() {
        return new OpenAPI().info(new Info()
            .title("社区服务系统后端 API")
            .description("基于 Spring Boot 3 的社区服务系统后端接口文档")
            .version("1.0.0")
            .contact(new Contact().name("社区服务系统").email("support@example.com"))
            .license(new License().name("Internal Use Only")));
    }
}

