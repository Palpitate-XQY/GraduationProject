package xxqqyyy.community.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页响应结构。
 *
 * @param <T> 数据类型
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应")
public class PageResult<T> {

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总条数")
    private long total;

    @Schema(description = "当前页")
    private long current;

    @Schema(description = "页大小")
    private long size;

    /**
     * 创建空分页。
     *
     * @param current 当前页
     * @param size    页大小
     * @param <T>     数据类型
     * @return 空分页对象
     */
    public static <T> PageResult<T> empty(long current, long size) {
        return PageResult.<T>builder()
            .records(Collections.emptyList())
            .total(0L)
            .current(current)
            .size(size)
            .build();
    }
}

