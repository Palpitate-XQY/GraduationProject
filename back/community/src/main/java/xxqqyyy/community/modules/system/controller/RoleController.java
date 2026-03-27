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
import xxqqyyy.community.modules.system.dto.RoleCreateRequest;
import xxqqyyy.community.modules.system.dto.RoleUpdateRequest;
import xxqqyyy.community.modules.system.service.RoleService;
import xxqqyyy.community.modules.system.vo.RoleVO;

/**
 * 角色管理控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "角色分页查询")
    @GetMapping
    @PreAuthorize("hasAuthority('sys:role:list')")
    public ApiResponse<PageResult<RoleVO>> page(
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "current", defaultValue = "1") long current,
        @RequestParam(value = "size", defaultValue = "10") long size
    ) {
        return ApiResponse.success(roleService.page(keyword, current, size));
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:view')")
    public ApiResponse<RoleVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(roleService.getById(id));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("@roleAuthorizationService.canCreateRole()")
    @OperationLog(module = "角色", type = "新增")
    public ApiResponse<Void> create(@Valid @RequestBody RoleCreateRequest request) {
        roleService.create(request);
        return ApiResponse.success();
    }

    @Operation(summary = "更新角色")
    @PutMapping
    @PreAuthorize("hasAuthority('sys:role:update')")
    @OperationLog(module = "角色", type = "编辑")
    public ApiResponse<Void> update(@Valid @RequestBody RoleUpdateRequest request) {
        roleService.update(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    @OperationLog(module = "角色", type = "删除")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        roleService.delete(id);
        return ApiResponse.success();
    }
}
