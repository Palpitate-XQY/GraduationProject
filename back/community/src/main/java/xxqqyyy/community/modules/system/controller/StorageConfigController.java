package xxqqyyy.community.modules.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.modules.log.annotation.OperationLog;
import xxqqyyy.community.modules.system.dto.StorageConfigUpdateRequest;
import xxqqyyy.community.modules.system.service.StorageConfigService;
import xxqqyyy.community.modules.system.vo.StorageConfigVO;

/**
 * 文件存储配置控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@Tag(name = "文件存储配置")
@RestController
@RequestMapping("/api/system/storage-config")
@RequiredArgsConstructor
public class StorageConfigController {

    private final StorageConfigService storageConfigService;

    @Operation(summary = "查询当前文件存储配置（仅超级管理员）")
    @GetMapping
    @PreAuthorize("hasAuthority('sys:storage:view')")
    public ApiResponse<StorageConfigVO> current() {
        return ApiResponse.success(storageConfigService.currentForAdmin());
    }

    @Operation(summary = "更新文件存储配置（仅超级管理员）")
    @PutMapping
    @PreAuthorize("hasAuthority('sys:storage:update')")
    @OperationLog(module = "文件存储配置", type = "更新")
    public ApiResponse<Void> update(@Valid @RequestBody StorageConfigUpdateRequest request) {
        storageConfigService.update(request);
        return ApiResponse.success();
    }
}

