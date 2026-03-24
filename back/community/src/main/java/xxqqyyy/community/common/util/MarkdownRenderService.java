package xxqqyyy.community.common.util;

import org.apache.commons.lang3.StringUtils;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

/**
 * Markdown 渲染服务，统一将 Markdown 转换为安全 HTML。
 *
 * @author codex
 * @since 1.0.0
 */
@Component
public class MarkdownRenderService {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder()
        .escapeHtml(true)
        .sanitizeUrls(true)
        .build();

    /**
     * 将 Markdown 文本渲染为 HTML。
     *
     * @param markdown Markdown 内容
     * @return HTML 内容
     */
    public String renderToHtml(String markdown) {
        if (StringUtils.isBlank(markdown)) {
            return "";
        }
        return htmlRenderer.render(parser.parse(markdown));
    }
}

