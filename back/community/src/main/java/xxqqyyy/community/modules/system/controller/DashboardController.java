package xxqqyyy.community.modules.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.modules.system.service.DashboardService;
import xxqqyyy.community.modules.system.vo.DashboardOverviewVO;

/**
 * 首页看板控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@Tag(name = "首页看板")
@RestController
@RequestMapping("/api/system/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "看板总览")
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public ApiResponse<DashboardOverviewVO> overview() {
        return ApiResponse.success(dashboardService.overview());
    }
}
