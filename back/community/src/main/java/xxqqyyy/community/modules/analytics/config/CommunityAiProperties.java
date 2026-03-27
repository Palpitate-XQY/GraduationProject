package xxqqyyy.community.modules.analytics.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI-compatible AI client properties.
 */
@Data
@ConfigurationProperties(prefix = "community.ai")
public class CommunityAiProperties {

    /**
     * Whether AI generation is enabled.
     */
    private boolean enabled = false;

    /**
     * OpenAI-compatible base URL.
     */
    private String baseUrl = "https://api.openai.com/v1";

    /**
     * API key.
     */
    private String apiKey;

    /**
     * Chat model name.
     */
    private String model = "gpt-4o-mini";

    /**
     * Request timeout in milliseconds.
     */
    private long timeoutMs = 10000;
}
