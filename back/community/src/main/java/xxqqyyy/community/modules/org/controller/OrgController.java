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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.modules.log.annotation.OperationLog;
import xxqqyyy.community.modules.org.dto.OrgCreateRequest;
import xxqqyyy.community.modules.org.dto.OrgQuery;
import xxqqyyy.community.modules.org.dto.OrgUpdateRequest;
import xxqqyyy.community.modules.org.service.OrgService;
import xxqqyyy.community.modules.org.vo.OrgTreeVO;

/**
 * 组织管理控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@Tag(name = "组织管理")
@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @Operation(summary = "查询组织树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('org:tree:view')")
    public ApiResponse<List<OrgTreeVO>> tree(@Valid OrgQuery query) {
        return ApiResponse.success(orgService.tree(query));
    }

    @Operation(summary = "新增组织")
    @PostMapping
    @PreAuthorize("hasAuthority('org:create')")
    @OperationLog(module = "组织", type = "新增")
    public ApiResponse<Void> create(@Valid @RequestBody OrgCreateRequest request) {
        orgService.create(request);
        return ApiResponse.success();
    }

    @Operation(summary = "更新组织")
    @PutMapping
    @PreAuthorize("hasAuthority('org:update')")
    @OperationLog(module = "组织", type = "编辑")
    public ApiResponse<Void> update(@Valid @RequestBody OrgUpdateRequest request) {
        orgService.update(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除组织")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('org:delete')")
    @OperationLog(module = "组织", type = "删除")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        orgService.delete(id);
        return ApiResponse.success();
    }
}
