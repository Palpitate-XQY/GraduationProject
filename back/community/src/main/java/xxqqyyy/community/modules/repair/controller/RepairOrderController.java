package xxqqyyy.community.modules.repair.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.log.annotation.OperationLog;
import xxqqyyy.community.modules.repair.dto.RepairAcceptRequest;
import xxqqyyy.community.modules.repair.dto.RepairAssignRequest;
import xxqqyyy.community.modules.repair.dto.RepairCloseRequest;
import xxqqyyy.community.modules.repair.dto.RepairConfirmRequest;
import xxqqyyy.community.modules.repair.dto.RepairCreateRequest;
import xxqqyyy.community.modules.repair.dto.RepairEvaluateRequest;
import xxqqyyy.community.modules.repair.dto.RepairPageQuery;
import xxqqyyy.community.modules.repair.dto.RepairProcessRequest;
import xxqqyyy.community.modules.repair.dto.RepairRejectRequest;
import xxqqyyy.community.modules.repair.dto.RepairReopenRequest;
import xxqqyyy.community.modules.repair.dto.RepairSubmitRequest;
import xxqqyyy.community.modules.repair.dto.RepairTakeRequest;
import xxqqyyy.community.modules.repair.dto.RepairUrgeRequest;
import xxqqyyy.community.modules.repair.service.RepairOrderService;
import xxqqyyy.community.modules.repair.vo.RepairOrderLogVO;
import xxqqyyy.community.modules.repair.vo.RepairOrderVO;

/**
 * 报修工单控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/repairs")
@RequiredArgsConstructor
@Tag(name = "报修工单")
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @PostMapping
    @PreAuthorize("hasAuthority('repair:create')")
    @Operation(summary = "居民创建报修")
    @OperationLog(module = "报修", type = "创建")
    public ApiResponse<Void> create(@Valid @RequestBody RepairCreateRequest request) {
        repairOrderService.create(request);
        return ApiResponse.success();
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('repair:my:list')")
    @Operation(summary = "居民查看我的报修列表")
    public ApiResponse<PageResult<RepairOrderVO>> myPage(@Valid RepairPageQuery query) {
        return ApiResponse.success(repairOrderService.myPage(query));
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasAuthority('repair:my:view')")
    @Operation(summary = "居民查看我的报修详情")
    public ApiResponse<RepairOrderVO> myDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(repairOrderService.myDetail(id));
    }

    @GetMapping("/manage")
    @PreAuthorize("hasAuthority('repair:manage:list')")
    @Operation(summary = "物业查看工单列表")
    public ApiResponse<PageResult<RepairOrderVO>> managePage(@Valid RepairPageQuery query) {
        return ApiResponse.success(repairOrderService.managePage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('repair:manage:view')")
    @Operation(summary = "物业查看工单详情")
    public ApiResponse<RepairOrderVO> manageDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(repairOrderService.manageDetail(id));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAuthority('repair:accept')")
    @Operation(summary = "物业受理工单")
    @OperationLog(module = "报修", type = "受理")
    public ApiResponse<Void> accept(@PathVariable("id") Long id, @RequestBody(required = false) RepairAcceptRequest request) {
        repairOrderService.accept(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('repair:reject')")
    @Operation(summary = "物业驳回工单")
    @OperationLog(module = "报修", type = "驳回")
    public ApiResponse<Void> reject(@PathVariable("id") Long id, @Valid @RequestBody RepairRejectRequest request) {
        repairOrderService.reject(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('repair:assign')")
    @Operation(summary = "物业分派工单")
    @OperationLog(module = "报修", type = "分派")
    public ApiResponse<Void> assign(@PathVariable("id") Long id, @Valid @RequestBody RepairAssignRequest request) {
        repairOrderService.assign(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/take")
    @PreAuthorize("hasAuthority('repair:take')")
    @Operation(summary = "维修员接单")
    @OperationLog(module = "报修", type = "接单")
    public ApiResponse<Void> take(@PathVariable("id") Long id, @RequestBody(required = false) RepairTakeRequest request) {
        repairOrderService.take(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAuthority('repair:process')")
    @Operation(summary = "维修员处理工单")
    @OperationLog(module = "报修", type = "处理")
    public ApiResponse<Void> process(@PathVariable("id") Long id, @Valid @RequestBody RepairProcessRequest request) {
        repairOrderService.process(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('repair:submit')")
    @Operation(summary = "维修员提交处理结果")
    @OperationLog(module = "报修", type = "提交结果")
    public ApiResponse<Void> submit(@PathVariable("id") Long id, @Valid @RequestBody RepairSubmitRequest request) {
        repairOrderService.submit(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('repair:confirm')")
    @Operation(summary = "居民确认解决")
    @OperationLog(module = "报修", type = "确认解决")
    public ApiResponse<Void> confirm(@PathVariable("id") Long id, @RequestBody(required = false) RepairConfirmRequest request) {
        repairOrderService.confirm(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAuthority('repair:reopen')")
    @Operation(summary = "居民反馈未解决")
    @OperationLog(module = "报修", type = "未解决反馈")
    public ApiResponse<Void> reopen(@PathVariable("id") Long id, @Valid @RequestBody RepairReopenRequest request) {
        repairOrderService.reopen(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/evaluate")
    @PreAuthorize("hasAuthority('repair:evaluate')")
    @Operation(summary = "居民评价")
    @OperationLog(module = "报修", type = "评价")
    public ApiResponse<Void> evaluate(@PathVariable("id") Long id, @Valid @RequestBody RepairEvaluateRequest request) {
        repairOrderService.evaluate(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/urge")
    @PreAuthorize("hasAuthority('repair:urge')")
    @Operation(summary = "居民催单")
    @OperationLog(module = "报修", type = "催单")
    public ApiResponse<Void> urge(@PathVariable("id") Long id, @Valid @RequestBody RepairUrgeRequest request) {
        repairOrderService.urge(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('repair:close')")
    @Operation(summary = "物业关闭工单")
    @OperationLog(module = "报修", type = "关闭")
    public ApiResponse<Void> close(@PathVariable("id") Long id, @RequestBody(required = false) RepairCloseRequest request) {
        repairOrderService.close(id, request);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('repair:log:view')")
    @Operation(summary = "查看工单流转日志")
    public ApiResponse<List<RepairOrderLogVO>> logs(@PathVariable("id") Long id) {
        return ApiResponse.success(repairOrderService.flowLogs(id));
    }
}
