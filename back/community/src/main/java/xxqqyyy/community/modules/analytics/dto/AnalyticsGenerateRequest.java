package xxqqyyy.community.modules.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Manual report generation request.
 */
@Data
@Schema(description = "手动生成分析报告请求")
public class AnalyticsGenerateRequest {

    @Pattern(regexp = "^(DAILY|WEEKLY)$", message = "periodType 仅支持 DAILY/WEEKLY")
    @Schema(description = "报告类型：DAILY/WEEKLY")
    private String periodType = "DAILY";

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "锚点日期，默认今天")
    private LocalDate anchorDate;

    @Schema(description = "是否强制重算")
    private Boolean force = Boolean.FALSE;
}

