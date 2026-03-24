package xxqqyyy.community.modules.system.controller;

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
import xxqqyyy.community.modules.log.annotation.OperationLog;
import xxqqyyy.community.modules.system.dto.DictDataCreateRequest;
import xxqqyyy.community.modules.system.dto.DictDataPageQuery;
import xxqqyyy.community.modules.system.dto.DictDataUpdateRequest;
import xxqqyyy.community.modules.system.dto.DictTypeCreateRequest;
import xxqqyyy.community.modules.system.dto.DictTypePageQuery;
import xxqqyyy.community.modules.system.dto.DictTypeUpdateRequest;
import xxqqyyy.community.modules.system.service.DictService;
import xxqqyyy.community.modules.system.vo.DictDataVO;
import xxqqyyy.community.modules.system.vo.DictOptionVO;
import xxqqyyy.community.modules.system.vo.DictTypeVO;

/**
 * 字典管理控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/api/system/dicts")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @Operation(summary = "字典类型分页查询")
    @GetMapping("/types")
    @PreAuthorize("hasAuthority('sys:dict:type:list')")
    public ApiResponse<PageResult<DictTypeVO>> pageType(@Valid DictTypePageQuery query) {
        return ApiResponse.success(dictService.pageType(query));
    }

    @Operation(summary = "字典类型详情")
    @GetMapping("/types/{id}")
    @PreAuthorize("hasAuthority('sys:dict:type:view')")
    public ApiResponse<DictTypeVO> typeDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(dictService.getTypeById(id));
    }

    @Operation(summary = "新增字典类型")
    @PostMapping("/types")
    @PreAuthorize("hasAuthority('sys:dict:type:create')")
    @OperationLog(module = "字典管理", type = "新增字典类型")
    public ApiResponse<Void> createType(@Valid @RequestBody DictTypeCreateRequest request) {
        dictService.createType(request);
        return ApiResponse.success();
    }

    @Operation(summary = "更新字典类型")
    @PutMapping("/types")
    @PreAuthorize("hasAuthority('sys:dict:type:update')")
    @OperationLog(module = "字典管理", type = "更新字典类型")
    public ApiResponse<Void> updateType(@Valid @RequestBody DictTypeUpdateRequest request) {
        dictService.updateType(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasAuthority('sys:dict:type:delete')")
    @OperationLog(module = "字典管理", type = "删除字典类型")
    public ApiResponse<Void> deleteType(@PathVariable("id") Long id) {
        dictService.deleteType(id);
        return ApiResponse.success();
    }

    @Operation(summary = "字典数据分页查询")
    @GetMapping("/data")
    @PreAuthorize("hasAuthority('sys:dict:data:list')")
    public ApiResponse<PageResult<DictDataVO>> pageData(@Valid DictDataPageQuery query) {
        return ApiResponse.success(dictService.pageData(query));
    }

    @Operation(summary = "字典数据详情")
    @GetMapping("/data/{id}")
    @PreAuthorize("hasAuthority('sys:dict:data:view')")
    public ApiResponse<DictDataVO> dataDetail(@PathVariable("id") Long id) {
        return ApiResponse.success(dictService.getDataById(id));
    }

    @Operation(summary = "新增字典数据")
    @PostMapping("/data")
    @PreAuthorize("hasAuthority('sys:dict:data:create')")
    @OperationLog(module = "字典管理", type = "新增字典数据")
    public ApiResponse<Void> createData(@Valid @RequestBody DictDataCreateRequest request) {
        dictService.createData(request);
        return ApiResponse.success();
    }

    @Operation(summary = "更新字典数据")
    @PutMapping("/data")
    @PreAuthorize("hasAuthority('sys:dict:data:update')")
    @OperationLog(module = "字典管理", type = "更新字典数据")
    public ApiResponse<Void> updateData(@Valid @RequestBody DictDataUpdateRequest request) {
        dictService.updateData(request);
        return ApiResponse.success();
    }

    @Operation(summary = "删除字典数据")
    @DeleteMapping("/data/{id}")
    @PreAuthorize("hasAuthority('sys:dict:data:delete')")
    @OperationLog(module = "字典管理", type = "删除字典数据")
    public ApiResponse<Void> deleteData(@PathVariable("id") Long id) {
        dictService.deleteData(id);
        return ApiResponse.success();
    }

    @Operation(summary = "根据字典编码获取启用选项")
    @GetMapping("/options/{dictCode}")
    @PreAuthorize("hasAuthority('sys:dict:option:list')")
    public ApiResponse<List<DictOptionVO>> options(@PathVariable("dictCode") String dictCode) {
        return ApiResponse.success(dictService.listOptions(dictCode));
    }
}
