package xxqqyyy.community.modules.activity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.activity.dto.ActivityCreateRequest;
import xxqqyyy.community.modules.activity.dto.ActivityPageQuery;
import xxqqyyy.community.modules.activity.dto.ActivityUpdateRequest;
import xxqqyyy.community.modules.activity.service.ActivityService;
import xxqqyyy.community.modules.activity.vo.ActivitySignupVO;
import xxqqyyy.community.modules.activity.vo.ActivityStatsVO;
import xxqqyyy.community.modules.activity.vo.ActivityVO;
import xxqqyyy.community.modules.log.annotation.OperationLog;

/**
 * 活动模块控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "活动管理")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    @PreAuthorize("hasAuthority('activity:list')")
    @Operation(summary = "活动管理分页")
    public ApiResponse<PageResult<ActivityVO>> page(@Valid ActivityPageQuery query) {
        return ApiResponse.success(activityService.managePage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('activity:view')")
    @Operation(summary = "活动详情")
    public ApiResponse<ActivityVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(activityService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('activity:create')")
    @OperationLog(module = "活动", type = "新增")
    @Operation(summary = "新增活动")
    public ApiResponse<Void> create(@Valid @RequestBody ActivityCreateRequest request) {
        activityService.create(request);
        return ApiResponse.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('activity:update')")
    @OperationLog(module = "活动", type = "更新")
    @Operation(summary = "更新活动")
    public ApiResponse<Void> update(@Valid @RequestBody ActivityUpdateRequest request) {
        activityService.update(request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('activity:delete')")
    @OperationLog(module = "活动", type = "删除")
    @Operation(summary = "删除活动")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        activityService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('activity:publish')")
    @OperationLog(module = "活动", type = "发布")
    @Operation(summary = "发布活动")
    public ApiResponse<Void> publish(@PathVariable("id") Long id) {
        activityService.publish(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/recall")
    @PreAuthorize("hasAuthority('activity:recall')")
    @OperationLog(module = "活动", type = "撤回")
    @Operation(summary = "撤回活动")
    public ApiResponse<Void> recall(@PathVariable("id") Long id) {
        activityService.recall(id);
        return ApiResponse.success();
    }

    @GetMapping("/resident/page")
    @PreAuthorize("hasAuthority('activity:resident:list')")
    @Operation(summary = "居民活动分页")
    public ApiResponse<PageResult<ActivityVO>> residentPage(@Valid ActivityPageQuery query) {
        return ApiResponse.success(activityService.residentPage(query));
    }

    @GetMapping("/resident/{id}")
    @PreAuthorize("hasAuthority('activity:resident:view')")
    @Operation(summary = "居民活动详情")
    public ApiResponse<ActivityVO> residentDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(activityService.residentDetail(id));
    }

    @PostMapping("/{id}/signup")
    @PreAuthorize("hasAuthority('activity:signup')")
    @OperationLog(module = "活动", type = "报名")
    @Operation(summary = "居民报名活动")
    public ApiResponse<Void> signup(@PathVariable("id") Long id) {
        activityService.signup(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/cancel-signup")
    @PreAuthorize("hasAuthority('activity:signup')")
    @OperationLog(module = "活动", type = "取消报名")
    @Operation(summary = "居民取消报名")
    public ApiResponse<Void> cancelSignup(@PathVariable("id") Long id) {
        activityService.cancelSignup(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/signups")
    @PreAuthorize("hasAuthority('activity:signup:list')")
    @Operation(summary = "活动报名名单")
    public ApiResponse<List<ActivitySignupVO>> signupList(@PathVariable("id") Long id) {
        return ApiResponse.success(activityService.signupList(id));
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAuthority('activity:stats')")
    @Operation(summary = "活动报名统计")
    public ApiResponse<ActivityStatsVO> stats(@PathVariable("id") Long id) {
        return ApiResponse.success(activityService.stats(id));
    }
}
