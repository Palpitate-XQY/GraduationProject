package xxqqyyy.community.modules.notice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import xxqqyyy.community.modules.log.annotation.OperationLog;
import xxqqyyy.community.modules.notice.dto.NoticeCreateRequest;
import xxqqyyy.community.modules.notice.dto.NoticePageQuery;
import xxqqyyy.community.modules.notice.dto.NoticeUpdateRequest;
import xxqqyyy.community.modules.notice.service.NoticeService;
import xxqqyyy.community.modules.notice.vo.NoticeVO;

/**
 * 公告模块控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@Tag(name = "公告管理")
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "公告管理分页")
    @GetMapping
    @PreAuthorize("hasAuthority('notice:list')")
    public ApiResponse<PageResult<NoticeVO>> page(@Valid NoticePageQuery query) {
        return ApiResponse.success(noticeService.managePage(query));
    }

    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('notice:view')")
    public ApiResponse<NoticeVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(noticeService.detail(id));
    }

    @Operation(summary = "新增公告")
    @PostMapping
    @PreAuthorize("hasAuthority('notice:create')")
    @OperationLog(module = "公告", type = "新增")
    public ApiResponse<Void> create(@Valid @RequestBody NoticeCreateRequest request) {
        noticeService.create(request);
        return ApiResponse.success();
    }

    @Operation(summary = "更新公告")
    @PutMapping
    @PreAuthorize("hasAuthority('notice:update')")
    @OperationLog(module = "公告", type = "更新")
    public ApiResponse<Void> update(@Valid @RequestBody NoticeUpdateRequest request) {
        noticeService.update(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('notice:delete')")
    @OperationLog(module = "公告", type = "删除")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        noticeService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "发布公告")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('notice:publish')")
    @OperationLog(module = "公告", type = "发布")
    public ApiResponse<Void> publish(@PathVariable("id") Long id) {
        noticeService.publish(id);
        return ApiResponse.success();
    }

    @Operation(summary = "撤回公告")
    @PostMapping("/{id}/recall")
    @PreAuthorize("hasAuthority('notice:recall')")
    @OperationLog(module = "公告", type = "撤回")
    public ApiResponse<Void> recall(@PathVariable("id") Long id) {
        noticeService.recall(id);
        return ApiResponse.success();
    }

    @Operation(summary = "居民公告分页")
    @GetMapping("/resident/page")
    @PreAuthorize("hasAuthority('notice:resident:list')")
    public ApiResponse<PageResult<NoticeVO>> residentPage(@Valid NoticePageQuery query) {
        return ApiResponse.success(noticeService.residentPage(query));
    }

    @Operation(summary = "居民公告详情")
    @GetMapping("/resident/{id}")
    @PreAuthorize("hasAuthority('notice:resident:view')")
    public ApiResponse<NoticeVO> residentDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(noticeService.residentDetail(id));
    }
}

