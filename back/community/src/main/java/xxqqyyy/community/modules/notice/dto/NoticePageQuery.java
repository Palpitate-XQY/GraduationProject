package xxqqyyy.community.modules.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 公告分页查询参数。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Schema(description = "公告分页查询参数")
public class NoticePageQuery {

    @Min(value = 1, message = "当前页最小为1")
    private long current = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private long size = 10;

    @Schema(description = "关键字（标题）")
    private String keyword;

    @Schema(description = "公告类型")
    private String noticeType;

    @Schema(description = "公告状态")
    private String status;
}

