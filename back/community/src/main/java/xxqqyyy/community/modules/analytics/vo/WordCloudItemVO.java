package xxqqyyy.community.modules.analytics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Word cloud item.
 */
@Data
@Builder
@Schema(description = "词云词项")
public class WordCloudItemVO {

    @Schema(description = "词语")
    private String word;

    @Schema(description = "权重")
    private long weight;
}

