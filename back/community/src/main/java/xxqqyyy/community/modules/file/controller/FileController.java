package xxqqyyy.community.modules.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xxqqyyy.community.common.api.ApiResponse;
import xxqqyyy.community.modules.file.service.FileService;
import xxqqyyy.community.modules.file.vo.FileInfoVO;
import xxqqyyy.community.modules.log.annotation.OperationLog;

/**
 * File upload and access controller.
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(
        "hasAnyAuthority(" +
            "'file:upload'," +
            "'repair:create'," +
            "'repair:process'," +
            "'repair:submit'," +
            "'repair:reopen'" +
        ")"
    )
    @OperationLog(module = "文件", type = "上传")
    public ApiResponse<FileInfoVO> upload(
        @Parameter(description = "待上传文件", required = true)
        @RequestParam("file") MultipartFile file,
        @Parameter(description = "业务类型")
        @RequestParam(value = "bizType", required = false) String bizType,
        @Parameter(description = "业务ID")
        @RequestParam(value = "bizId", required = false) Long bizId
    ) {
        return ApiResponse.success(fileService.upload(file, bizType, bizId));
    }

    @Operation(summary = "查询文件详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('file:view')")
    public ApiResponse<FileInfoVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(fileService.getById(id));
    }

    @Operation(summary = "下载文件（本地存储直出，七牛存储302跳转）")
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('file:view')")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) {
        fileService.download(id, response);
    }

    @Operation(summary = "预览文件（用于公告/活动附件与图片）")
    @GetMapping("/{id}/preview")
    @PreAuthorize("isAuthenticated()")
    public void preview(@PathVariable("id") Long id, HttpServletResponse response) {
        fileService.preview(id, response);
    }
}
