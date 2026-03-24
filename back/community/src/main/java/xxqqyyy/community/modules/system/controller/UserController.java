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
import xxqqyyy.community.modules.system.dto.UserCreateRequest;
import xxqqyyy.community.modules.system.dto.UserPageQuery;
import xxqqyyy.community.modules.system.dto.UserRoleAssignRequest;
import xxqqyyy.community.modules.system.dto.UserUpdateRequest;
import xxqqyyy.community.modules.log.annotation.OperationLog;
import xxqqyyy.community.modules.system.service.UserService;
import xxqqyyy.community.modules.system.vo.UserVO;

/**
 * 用户管理控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户分页查询")
    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:list')")
    public ApiResponse<PageResult<UserVO>> page(@Valid UserPageQuery query) {
        return ApiResponse.success(userService.page(query));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:view')")
    public ApiResponse<UserVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:create')")
    @OperationLog(module = "用户", type = "新增")
    public ApiResponse<Void> create(@Valid @RequestBody UserCreateRequest request) {
        userService.create(request);
        return ApiResponse.success();
    }

    @Operation(summary = "编辑用户")
    @PutMapping
    @PreAuthorize("hasAuthority('sys:user:update')")
    @OperationLog(module = "用户", type = "编辑")
    public ApiResponse<Void> update(@Valid @RequestBody UserUpdateRequest request) {
        userService.update(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除用户（逻辑删除）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    @OperationLog(module = "用户", type = "删除")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sys:user:update')")
    @OperationLog(module = "用户", type = "状态变更")
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        userService.updateStatus(id, status);
        return ApiResponse.success();
    }

    @Operation(summary = "分配用户角色")
    @PutMapping("/roles")
    @PreAuthorize("hasAuthority('sys:user:assign-role')")
    @OperationLog(module = "用户", type = "分配角色")
    public ApiResponse<Void> assignRoles(@Valid @RequestBody UserRoleAssignRequest request) {
        userService.assignRoles(request);
        return ApiResponse.success();
    }
}
