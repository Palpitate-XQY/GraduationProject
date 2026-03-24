package xxqqyyy.community.modules.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * 首页看板总览视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "首页看板总览")
public class DashboardOverviewVO {

    @Schema(description = "看板编码")
    private String dashboardCode;

    @Schema(description = "页面包编码")
    private String pagePackCode;

    @Schema(description = "默认首页路由")
    private String defaultHomeRoute;

    @Schema(description = "当前用户角色编码集合")
    private Set<String> roleCodes;

    @Schema(description = "当前用户权限编码集合")
    private Set<String> permissionCodes;

    @Schema(description = "看板卡片列表")
    private List<DashboardCardVO> cards;
}
