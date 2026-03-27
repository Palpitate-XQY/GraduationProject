package xxqqyyy.community.modules.file.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
import xxqqyyy.community.modules.activity.entity.BizActivity;
import xxqqyyy.community.modules.activity.entity.BizActivityScope;
import xxqqyyy.community.modules.activity.enums.ActivityStatusEnum;
import xxqqyyy.community.modules.activity.mapper.BizActivityMapper;
import xxqqyyy.community.modules.activity.mapper.BizActivityScopeMapper;
import xxqqyyy.community.modules.file.entity.BizFileInfo;
import xxqqyyy.community.modules.file.enums.FileStorageTypeEnum;
import xxqqyyy.community.modules.file.mapper.BizFileInfoMapper;
import xxqqyyy.community.modules.file.service.FileService;
import xxqqyyy.community.modules.file.vo.FileInfoVO;
import xxqqyyy.community.modules.notice.entity.BizNotice;
import xxqqyyy.community.modules.notice.entity.BizNoticeScope;
import xxqqyyy.community.modules.notice.enums.NoticeStatusEnum;
import xxqqyyy.community.modules.notice.mapper.BizNoticeMapper;
import xxqqyyy.community.modules.notice.mapper.BizNoticeScopeMapper;
import xxqqyyy.community.modules.org.entity.SysOrg;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.modules.system.service.StorageConfigService;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * File service implementation.
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private static final String BIZ_TYPE_NOTICE = "NOTICE";
    private static final String BIZ_TYPE_ACTIVITY = "ACTIVITY";
    private static final String BIZ_TYPE_UNBOUND = "UNBOUND";

    private static final List<String> NOTICE_MANAGE_PERMISSIONS = List.of(
        "notice:list",
        "notice:view",
        "notice:create",
        "notice:update",
        "notice:delete",
        "notice:publish",
        "notice:recall"
    );

    private static final List<String> NOTICE_RESIDENT_PERMISSIONS = List.of(
        "notice:resident:list",
        "notice:resident:view"
    );

    private static final List<String> ACTIVITY_MANAGE_PERMISSIONS = List.of(
        "activity:list",
        "activity:view",
        "activity:create",
        "activity:update",
        "activity:delete",
        "activity:publish",
        "activity:recall",
        "activity:stats",
        "activity:signup:list"
    );

    private static final List<String> ACTIVITY_RESIDENT_PERMISSIONS = List.of(
        "activity:resident:list",
        "activity:resident:view",
        "activity:signup"
    );

    private final BizFileInfoMapper bizFileInfoMapper;
    private final StorageConfigService storageConfigService;
    private final FileStorageRouter fileStorageRouter;
    private final FileStorageProperties fileStorageProperties;
    private final BizNoticeMapper bizNoticeMapper;
    private final BizNoticeScopeMapper bizNoticeScopeMapper;
    private final BizActivityMapper bizActivityMapper;
    private final BizActivityScopeMapper bizActivityScopeMapper;
    private final SysOrgMapper sysOrgMapper;
    private final DataScopeService dataScopeService;

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
        transferFile(fileInfo, response, true);
    }

    @Override
    public void preview(Long fileId, HttpServletResponse response) {
        BizFileInfo fileInfo = requireById(fileId);
        assertPreviewPermission(fileInfo);
        transferFile(fileInfo, response, false);
    }

    @Override
    public BizFileInfo requireById(Long fileId) {
        BizFileInfo fileInfo = bizFileInfoMapper.selectById(fileId);
        if (fileInfo == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return fileInfo;
    }

    private void transferFile(BizFileInfo fileInfo, HttpServletResponse response, boolean attachment) {
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
        writeResponse(response, resource, fileInfo, attachment);
    }

    private void writeResponse(HttpServletResponse response, Resource resource, BizFileInfo fileInfo, boolean attachment) {
        String contentType = StringUtils.defaultIfBlank(fileInfo.getContentType(), "application/octet-stream");
        response.setContentType(contentType);
        if (fileInfo.getFileSize() != null && fileInfo.getFileSize() > 0) {
            response.setContentLengthLong(fileInfo.getFileSize());
        }
        String encodedFileName = URLEncoder.encode(
            StringUtils.defaultIfBlank(fileInfo.getOriginFileName(), fileInfo.getFileName()),
            StandardCharsets.UTF_8
        );
        if (attachment) {
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        } else {
            response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName);
        }
        try (var inputStream = resource.getInputStream(); var outputStream = response.getOutputStream()) {
            inputStream.transferTo(outputStream);
            outputStream.flush();
        } catch (IOException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "文件下载失败: " + ex.getMessage());
        }
    }

    private void assertPreviewPermission(BizFileInfo fileInfo) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        if (principal.isSuperAdmin() || hasPermission(principal, "file:view")) {
            return;
        }

        if (isOwnUnboundFile(fileInfo, principal)) {
            return;
        }

        String bizType = StringUtils.upperCase(StringUtils.trimToEmpty(fileInfo.getBizType()), Locale.ROOT);
        Long bizId = fileInfo.getBizId();

        boolean allowed = switch (bizType) {
            case BIZ_TYPE_NOTICE -> canPreviewNotice(principal, bizId);
            case BIZ_TYPE_ACTIVITY -> canPreviewActivity(principal, bizId);
            default -> false;
        };

        if (!allowed) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该文件");
        }
    }

    private boolean isOwnUnboundFile(BizFileInfo fileInfo, LoginPrincipal principal) {
        if (fileInfo.getBizId() != null) {
            return false;
        }
        if (!StringUtils.equalsIgnoreCase(fileInfo.getBizType(), BIZ_TYPE_UNBOUND)) {
            return false;
        }
        return fileInfo.getUploaderId() != null && fileInfo.getUploaderId().equals(principal.getUserId());
    }

    private boolean canPreviewNotice(LoginPrincipal principal, Long noticeId) {
        if (noticeId == null) {
            return false;
        }
        BizNotice notice = bizNoticeMapper.selectById(noticeId);
        if (notice == null) {
            return false;
        }

        if (hasAnyPermission(principal, NOTICE_MANAGE_PERMISSIONS)) {
            return canAccessPublisherOrg(principal.getUserId(), notice.getPublisherOrgId());
        }
        if (!hasAnyPermission(principal, NOTICE_RESIDENT_PERMISSIONS)) {
            return false;
        }
        if (!NoticeStatusEnum.PUBLISHED.getCode().equals(notice.getStatus())) {
            return false;
        }
        return isNoticeVisibleToPrincipalOrg(noticeId, principal.getOrgId());
    }

    private boolean canPreviewActivity(LoginPrincipal principal, Long activityId) {
        if (activityId == null) {
            return false;
        }
        BizActivity activity = bizActivityMapper.selectById(activityId);
        if (activity == null) {
            return false;
        }

        if (hasAnyPermission(principal, ACTIVITY_MANAGE_PERMISSIONS)) {
            return canAccessPublisherOrg(principal.getUserId(), activity.getPublisherOrgId());
        }
        if (!hasAnyPermission(principal, ACTIVITY_RESIDENT_PERMISSIONS)) {
            return false;
        }
        if (!ActivityStatusEnum.PUBLISHED.getCode().equals(activity.getStatus())) {
            return false;
        }
        return isActivityVisibleToPrincipalOrg(activityId, principal.getOrgId());
    }

    private boolean canAccessPublisherOrg(Long userId, Long publisherOrgId) {
        try {
            dataScopeService.assertOrgAccessible(userId, publisherOrgId);
            return true;
        } catch (BizException ex) {
            return false;
        }
    }

    private boolean isNoticeVisibleToPrincipalOrg(Long noticeId, Long principalOrgId) {
        SysOrg org = queryOrg(principalOrgId);
        if (org == null) {
            return false;
        }
        List<BizNoticeScope> scopes = bizNoticeScopeMapper.selectByNoticeId(noticeId);
        return isVisibleToOrg(scopes.stream().map(BizNoticeScope::getScopeType).toList(),
            scopes.stream().map(BizNoticeScope::getScopeRefId).toList(),
            org.getId(),
            org.getAncestorPath());
    }

    private boolean isActivityVisibleToPrincipalOrg(Long activityId, Long principalOrgId) {
        SysOrg org = queryOrg(principalOrgId);
        if (org == null) {
            return false;
        }
        List<BizActivityScope> scopes = bizActivityScopeMapper.selectByActivityId(activityId);
        return isVisibleToOrg(scopes.stream().map(BizActivityScope::getScopeType).toList(),
            scopes.stream().map(BizActivityScope::getScopeRefId).toList(),
            org.getId(),
            org.getAncestorPath());
    }

    private SysOrg queryOrg(Long orgId) {
        if (orgId == null) {
            return null;
        }
        return sysOrgMapper.selectById(orgId);
    }

    private boolean isVisibleToOrg(List<String> scopeTypes, List<Long> scopeRefIds, Long orgId, String ancestorPath) {
        if (orgId == null || scopeTypes == null || scopeRefIds == null) {
            return false;
        }
        int size = Math.min(scopeTypes.size(), scopeRefIds.size());
        for (int i = 0; i < size; i++) {
            String scopeType = scopeTypes.get(i);
            Long scopeRefId = scopeRefIds.get(i);
            if ("ALL".equalsIgnoreCase(scopeType)) {
                return true;
            }
            if (scopeRefId == null) {
                continue;
            }
            if (scopeRefId.equals(orgId)) {
                return true;
            }
            if (ancestorPath != null && ancestorPath.contains("/" + scopeRefId + "/")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermission(LoginPrincipal principal, String permissionCode) {
        Set<String> permissionCodes = principal.getPermissionCodes();
        return permissionCodes != null && permissionCodes.contains(permissionCode);
    }

    private boolean hasAnyPermission(LoginPrincipal principal, List<String> targetPermissions) {
        Set<String> permissionCodes = principal.getPermissionCodes();
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return false;
        }
        for (String permission : targetPermissions) {
            if (permissionCodes.contains(permission)) {
                return true;
            }
        }
        return false;
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
