package xxqqyyy.community.modules.system.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.log.annotation.OperationLog;
import xxqqyyy.community.modules.system.dto.PermissionCreateRequest;
import xxqqyyy.community.modules.system.dto.PermissionUpdateRequest;
import xxqqyyy.community.modules.system.service.PermissionService;
import xxqqyyy.community.modules.system.vo.PermissionVO;

/**
 * 权限管理控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@Tag(name = "权限管理")
@RestController
@RequestMapping("/api/system/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "权限分页查询")
    @GetMapping
    @PreAuthorize("hasAuthority('sys:permission:list')")
    public ApiResponse<PageResult<PermissionVO>> page(
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "current", defaultValue = "1") long current,
        @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        return ApiResponse.success(permissionService.page(keyword, current, size));
    }

    @Operation(summary = "权限详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:permission:view')")
    public ApiResponse<PermissionVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(permissionService.getById(id));
    }

    @Operation(summary = "新增权限")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:permission:create')")
    @OperationLog(module = "权限", type = "新增")
    public ApiResponse<Void> create(@Valid @RequestBody PermissionCreateRequest request) {
        permissionService.create(request);
        return ApiResponse.success();
    }

    @Operation(summary = "更新权限")
    @PutMapping
    @PreAuthorize("hasAuthority('sys:permission:update')")
    @OperationLog(module = "权限", type = "编辑")
    public ApiResponse<Void> update(@Valid @RequestBody PermissionUpdateRequest request) {
        permissionService.update(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:permission:delete')")
    @OperationLog(module = "权限", type = "删除")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        permissionService.delete(id);
        return ApiResponse.success();
    }
}
