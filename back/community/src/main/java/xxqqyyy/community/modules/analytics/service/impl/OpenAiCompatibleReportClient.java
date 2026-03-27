package xxqqyyy.community.modules.analytics.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.analytics.config.CommunityAiProperties;
import xxqqyyy.community.modules.analytics.enums.AnalyticsAiModeEnum;
import xxqqyyy.community.modules.analytics.model.AiReportRequest;
import xxqqyyy.community.modules.analytics.model.AiReportResult;
import xxqqyyy.community.modules.analytics.service.AiReportClient;

/**
 * OpenAI-compatible chat completion client.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiCompatibleReportClient implements AiReportClient {

    private static final String SYSTEM_PROMPT = """
        你是“智慧社区运营分析助手”。请根据输入的结构化指标与词频数据输出中文 Markdown 报告。
        输出格式必须严格包含以下四个章节：
        1. 一、整体概览
        2. 二、热点词云解读
        3. 三、风险提醒
        4. 四、行动建议
        要求：
        - 每个章节 2-4 条要点，结论必须基于给定数据。
        - 禁止编造数据与事实，不输出与社区治理无关内容。
        - 语气专业、简洁、可执行。
        - 直接输出 Markdown 正文，不要输出额外前缀说明。
        """;

    private final CommunityAiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean isEnabled() {
        return aiProperties.isEnabled() && StringUtils.isNotBlank(aiProperties.getApiKey());
    }

    @Override
    public AiReportResult generate(AiReportRequest request) {
        if (!isEnabled()) {
            throw new BizException(400, "AI report client disabled");
        }
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(aiProperties.getTimeoutMs()))
                .build();

            String content = buildUserPrompt(request);
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", aiProperties.getModel());
            payload.put("temperature", 0.2);
            payload.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", content)
            ));

            String endpoint = normalizeBaseUrl(aiProperties.getBaseUrl()) + "/chat/completions";
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(aiProperties.getTimeoutMs()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + aiProperties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BizException(500, "AI API status: " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choicesNode = root.path("choices");
            if (!choicesNode.isArray() || choicesNode.isEmpty()) {
                throw new BizException(500, "AI response choices is empty");
            }
            String markdown = choicesNode.get(0).path("message").path("content").asText("");
            if (StringUtils.isBlank(markdown)) {
                throw new BizException(500, "AI response content is empty");
            }
            String model = root.path("model").asText(aiProperties.getModel());
            return AiReportResult.builder()
                .markdown(markdown.trim())
                .aiMode(AnalyticsAiModeEnum.AI.getCode())
                .modelName(model)
                .build();
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("OpenAI compatible report generation failed: {}", ex.getMessage());
            throw new BizException(500, "AI report generation failed");
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (StringUtils.isBlank(baseUrl)) {
            return "https://api.openai.com/v1";
        }
        String value = baseUrl.trim();
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String buildUserPrompt(AiReportRequest request) throws Exception {
        Map<String, Object> promptData = new HashMap<>();
        promptData.put("reportType", request.getReportType());
        promptData.put("scopeKind", request.getScopeKind());
        promptData.put("periodStart", request.getPeriodStart());
        promptData.put("periodEnd", request.getPeriodEnd());
        promptData.put("metrics", request.getMetrics());
        promptData.put("topWords", request.getTopWords());
        return "请基于以下 JSON 数据生成报告：\n```json\n"
            + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(promptData)
            + "\n```";
    }
}
