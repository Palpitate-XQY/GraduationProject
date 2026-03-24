package xxqqyyy.community.modules.log.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.log.dto.LoginLogPageQuery;
import xxqqyyy.community.modules.log.dto.OperationLogPageQuery;
import xxqqyyy.community.modules.log.service.LoginLogService;
import xxqqyyy.community.modules.log.service.OperationLogService;
import xxqqyyy.community.modules.log.vo.LoginLogVO;
import xxqqyyy.community.modules.log.vo.OperationLogVO;

/**
 * 日志查询控制器。
 *
 * @author codex
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "日志查询")
public class LogQueryController {

    private final LoginLogService loginLogService;
    private final OperationLogService operationLogService;

    @GetMapping("/logins")
    @PreAuthorize("hasAuthority('log:login:list')")
    @Operation(summary = "登录日志分页查询")
    public ApiResponse<PageResult<LoginLogVO>> loginPage(@Valid LoginLogPageQuery query) {
        return ApiResponse.success(loginLogService.page(query));
    }

    @GetMapping("/operations")
    @PreAuthorize("hasAuthority('log:operation:list')")
    @Operation(summary = "操作日志分页查询")
    public ApiResponse<PageResult<OperationLogVO>> operationPage(@Valid OperationLogPageQuery query) {
        return ApiResponse.success(operationLogService.page(query));
    }
}
