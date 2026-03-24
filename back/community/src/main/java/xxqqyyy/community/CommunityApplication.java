package xxqqyyy.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 社区服务系统后端启动类。
 *
 * @author codex
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("xxqqyyy.community.modules.**.mapper")
@ConfigurationPropertiesScan(basePackages = "xxqqyyy.community")
@EnableTransactionManagement
public class CommunityApplication {

    /**
     * 应用程序入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }
}

