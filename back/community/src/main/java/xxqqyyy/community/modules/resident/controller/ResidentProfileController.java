package xxqqyyy.community.modules.resident.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.modules.log.annotation.OperationLog;
import xxqqyyy.community.modules.resident.dto.ResidentProfileAdminUpdateRequest;
import xxqqyyy.community.modules.resident.dto.ResidentProfileMyUpdateRequest;
import xxqqyyy.community.modules.resident.service.ResidentProfileService;
import xxqqyyy.community.modules.resident.vo.ResidentProfileVO;

/**
 * 居民档案管理控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@Tag(name = "居民档案")
@RestController
@RequestMapping("/api/resident/profiles")
@RequiredArgsConstructor
public class ResidentProfileController {

    private final ResidentProfileService residentProfileService;

    @Operation(summary = "获取当前登录居民档案")
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('resident:profile:my:view')")
    public ApiResponse<ResidentProfileVO> myProfile() {
        return ApiResponse.success(residentProfileService.getMyProfile());
    }

    @Operation(summary = "更新当前登录居民档案")
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('resident:profile:my:update')")
    @OperationLog(module = "居民档案", type = "更新本人档案")
    public ApiResponse<Void> updateMyProfile(@Valid @RequestBody ResidentProfileMyUpdateRequest request) {
        residentProfileService.updateMyProfile(request);
        return ApiResponse.success();
    }

    @Operation(summary = "按用户ID查询居民档案")
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('resident:profile:view')")
    public ApiResponse<ResidentProfileVO> detail(@PathVariable("userId") Long userId) {
        return ApiResponse.success(residentProfileService.getByUserId(userId));
    }

    @Operation(summary = "管理端维护居民档案")
    @PutMapping
    @PreAuthorize("hasAuthority('resident:profile:update')")
    @OperationLog(module = "居民档案", type = "管理端维护")
    public ApiResponse<Void> upsertByAdmin(@Valid @RequestBody ResidentProfileAdminUpdateRequest request) {
        residentProfileService.upsertByAdmin(request);
        return ApiResponse.success();
    }
}
