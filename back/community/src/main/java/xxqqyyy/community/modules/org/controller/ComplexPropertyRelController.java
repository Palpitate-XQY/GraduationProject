package xxqqyyy.community.modules.org.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.modules.log.annotation.OperationLog;
import xxqqyyy.community.modules.org.dto.ComplexPropertyRelCreateRequest;
import xxqqyyy.community.modules.org.service.ComplexPropertyRelService;
import xxqqyyy.community.modules.org.vo.ComplexPropertyRelVO;

/**
 * 小区与物业关联管理控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@Tag(name = "小区物业关联管理")
@RestController
@RequestMapping("/api/org/complex-property-rel")
@RequiredArgsConstructor
public class ComplexPropertyRelController {

    private final ComplexPropertyRelService relService;

    @Operation(summary = "新增小区物业关联")
    @PostMapping
    @PreAuthorize("hasAuthority('org:complex-property:create')")
    @OperationLog(module = "组织", type = "新增小区物业关联")
    public ApiResponse<Void> create(@Valid @RequestBody ComplexPropertyRelCreateRequest request) {
        relService.create(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除小区物业关联")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('org:complex-property:delete')")
    @OperationLog(module = "组织", type = "删除小区物业关联")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        relService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "按小区查询物业关联")
    @GetMapping
    @PreAuthorize("hasAuthority('org:complex-property:list')")
    public ApiResponse<List<ComplexPropertyRelVO>> listByComplex(@RequestParam("complexOrgId") Long complexOrgId) {
        return ApiResponse.success(relService.listByComplex(complexOrgId));
    }
}
