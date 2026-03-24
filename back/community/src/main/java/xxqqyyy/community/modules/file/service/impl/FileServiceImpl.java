package xxqqyyy.community.modules.file.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.infrastructure.storage.FileStorageProperties;
import xxqqyyy.community.infrastructure.storage.FileStorageProvider;
import xxqqyyy.community.infrastructure.storage.FileStorageRouter;
import xxqqyyy.community.infrastructure.storage.model.StorageConfigSnapshot;
import xxqqyyy.community.infrastructure.storage.model.StoredFileResult;
import xxqqyyy.community.modules.file.entity.BizFileInfo;
import xxqqyyy.community.modules.file.enums.FileStorageTypeEnum;
import xxqqyyy.community.modules.file.mapper.BizFileInfoMapper;
import xxqqyyy.community.modules.file.service.FileService;
import xxqqyyy.community.modules.file.vo.FileInfoVO;
import xxqqyyy.community.modules.system.service.StorageConfigService;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 文件服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final BizFileInfoMapper bizFileInfoMapper;
    private final StorageConfigService storageConfigService;
    private final FileStorageRouter fileStorageRouter;
    private final FileStorageProperties fileStorageProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfoVO upload(MultipartFile file, String bizType, Long bizId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        validateUploadFile(file);

        StorageConfigSnapshot snapshot = storageConfigService.currentSnapshot();
        FileStorageTypeEnum storageType = resolveStorageType(snapshot.getStorageType());
        FileStorageProvider provider = fileStorageRouter.route(storageType);

        String originFileName = normalizeOriginFileName(file.getOriginalFilename());
        String objectKey = buildObjectKey(originFileName);
        StoredFileResult storeResult;
        try {
            storeResult = provider.store(file, objectKey, snapshot);
        } catch (IOException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件上传失败: " + ex.getMessage());
        }

        BizFileInfo fileInfo = new BizFileInfo();
        fileInfo.setBizType(StringUtils.trimToNull(bizType));
        fileInfo.setBizId(bizId);
        fileInfo.setStorageType(storageType.getCode());
        fileInfo.setFileName(Paths.get(objectKey).getFileName().toString());
        fileInfo.setOriginFileName(originFileName);
        fileInfo.setFilePath(storeResult.getFilePath());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setContentType(file.getContentType());
        fileInfo.setUploaderId(principal.getUserId());
        fileInfo.setCreateBy(principal.getUserId());
        fileInfo.setUpdateBy(principal.getUserId());
        bizFileInfoMapper.insert(fileInfo);

        return toVO(fileInfo, snapshot);
    }

    @Override
    public FileInfoVO getById(Long fileId) {
        BizFileInfo fileInfo = requireById(fileId);
        StorageConfigSnapshot snapshot = storageConfigService.currentSnapshot();
        return toVO(fileInfo, snapshot);
    }

    @Override
    public void download(Long fileId, HttpServletResponse response) {
        BizFileInfo fileInfo = requireById(fileId);
        StorageConfigSnapshot snapshot = storageConfigService.currentSnapshot();
        FileStorageTypeEnum storageType = resolveStorageType(fileInfo.getStorageType());
        FileStorageProvider provider = fileStorageRouter.route(storageType);
        String accessUrl = provider.buildAccessUrl(fileInfo.getFilePath(), fileInfo.getId(), snapshot);

        if (!provider.supportStreamDownload()) {
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.setHeader("Location", accessUrl);
            return;
        }

        Resource resource;
        try {
            resource = provider.loadAsResource(fileInfo.getFilePath(), snapshot);
        } catch (IOException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件读取失败: " + ex.getMessage());
        }
        writeDownloadResponse(response, resource, fileInfo);
    }

    @Override
    public BizFileInfo requireById(Long fileId) {
        BizFileInfo fileInfo = bizFileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return fileInfo;
    }

    private void writeDownloadResponse(HttpServletResponse response, Resource resource, BizFileInfo fileInfo) {
        String contentType = StringUtils.defaultIfBlank(fileInfo.getContentType(), "application/octet-stream");
        response.setContentType(contentType);
        if (fileInfo.getFileSize() != null && fileInfo.getFileSize() > 0) {
            response.setContentLengthLong(fileInfo.getFileSize());
        }
        String encodedFileName = URLEncoder.encode(
            StringUtils.defaultIfBlank(fileInfo.getOriginFileName(), fileInfo.getFileName()),
            StandardCharsets.UTF_8
        );
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        try (var inputStream = resource.getInputStream(); var outputStream = response.getOutputStream()) {
            inputStream.transferTo(outputStream);
            outputStream.flush();
        } catch (IOException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件下载失败: " + ex.getMessage());
        }
    }

    private FileInfoVO toVO(BizFileInfo fileInfo, StorageConfigSnapshot snapshot) {
        FileStorageTypeEnum storageType = resolveStorageType(fileInfo.getStorageType());
        FileStorageProvider provider = fileStorageRouter.route(storageType);
        return FileInfoVO.builder()
            .id(fileInfo.getId())
            .bizType(fileInfo.getBizType())
            .bizId(fileInfo.getBizId())
            .storageType(fileInfo.getStorageType())
            .fileName(fileInfo.getFileName())
            .originFileName(fileInfo.getOriginFileName())
            .fileSize(fileInfo.getFileSize())
            .contentType(fileInfo.getContentType())
            .accessUrl(provider.buildAccessUrl(fileInfo.getFilePath(), fileInfo.getId(), snapshot))
            .uploaderId(fileInfo.getUploaderId())
            .createTime(fileInfo.getCreateTime())
            .build();
    }

    private void validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "上传文件不能为空");
        }
        long maxFileSize = fileStorageProperties.getMaxFileSizeBytes();
        if (maxFileSize > 0 && file.getSize() > maxFileSize) {
            throw new BizException(ErrorCode.BAD_REQUEST, "文件大小超限，最大允许 " + maxFileSize + " 字节");
        }
    }

    private FileStorageTypeEnum resolveStorageType(String storageType) {
        String target = StringUtils.defaultIfBlank(storageType, fileStorageProperties.getDefaultType()).toUpperCase(Locale.ROOT);
        FileStorageTypeEnum typeEnum = FileStorageTypeEnum.fromCode(target);
        if (typeEnum == null) {
            throw new BizException(ErrorCode.BIZ_ERROR, "无效的文件存储类型: " + target);
        }
        return typeEnum;
    }

    private String normalizeOriginFileName(String originFileName) {
        if (StringUtils.isBlank(originFileName)) {
            return "unnamed";
        }
        String trimmed = originFileName.replace("\\", "/");
        String baseName = Paths.get(trimmed).getFileName().toString();
        if (StringUtils.isBlank(baseName)) {
            return "unnamed";
        }
        return baseName;
    }

    private String buildObjectKey(String originFileName) {
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);
        String ext = "";
        int index = originFileName.lastIndexOf('.');
        if (index > -1 && index < originFileName.length() - 1) {
            ext = originFileName.substring(index).toLowerCase(Locale.ROOT);
        }
        return datePath + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
    }
}

