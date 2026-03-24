package xxqqyyy.community.modules.file.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xxqqyyy.community.infrastructure.storage.FileStorageProperties;
import xxqqyyy.community.infrastructure.storage.FileStorageProvider;
import xxqqyyy.community.infrastructure.storage.FileStorageRouter;
import xxqqyyy.community.infrastructure.storage.model.StorageConfigSnapshot;
import xxqqyyy.community.modules.file.entity.BizFileInfo;
import xxqqyyy.community.modules.file.enums.FileStorageTypeEnum;
import xxqqyyy.community.modules.file.mapper.BizFileInfoMapper;
import xxqqyyy.community.modules.file.service.FileAttachmentService;
import xxqqyyy.community.modules.file.service.FileBindingService;
import xxqqyyy.community.modules.file.vo.FileAttachmentVO;
import xxqqyyy.community.modules.system.service.StorageConfigService;

/**
 * 业务附件查询服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class FileAttachmentServiceImpl implements FileAttachmentService {

    private final FileBindingService fileBindingService;
    private final BizFileInfoMapper bizFileInfoMapper;
    private final StorageConfigService storageConfigService;
    private final FileStorageRouter fileStorageRouter;
    private final FileStorageProperties fileStorageProperties;

    @Override
    public List<FileAttachmentVO> listByAttachmentJson(String attachmentJson) {
        Set<Long> fileIds = new LinkedHashSet<>(fileBindingService.collectFileIds(null, attachmentJson));
        if (fileIds.isEmpty()) {
            return List.of();
        }
        List<BizFileInfo> files = bizFileInfoMapper.selectByIds(fileIds);
        if (files.isEmpty()) {
            return List.of();
        }
        Map<Long, BizFileInfo> fileMap = files.stream().collect(Collectors.toMap(BizFileInfo::getId, item -> item));
        StorageConfigSnapshot snapshot = storageConfigService.currentSnapshot();
        List<FileAttachmentVO> result = new ArrayList<>();
        for (Long fileId : fileIds) {
            BizFileInfo fileInfo = fileMap.get(fileId);
            if (fileInfo == null) {
                continue;
            }
            FileStorageTypeEnum storageType = resolveStorageType(fileInfo.getStorageType(), snapshot.getStorageType());
            FileStorageProvider provider = fileStorageRouter.route(storageType);
            result.add(FileAttachmentVO.builder()
                .fileId(fileInfo.getId())
                .originFileName(fileInfo.getOriginFileName())
                .fileName(fileInfo.getFileName())
                .fileSize(fileInfo.getFileSize())
                .contentType(fileInfo.getContentType())
                .storageType(storageType.getCode())
                .accessUrl(provider.buildAccessUrl(fileInfo.getFilePath(), fileInfo.getId(), snapshot))
                .build());
        }
        return result;
    }

    private FileStorageTypeEnum resolveStorageType(String fileStorageType, String defaultStorageType) {
        String target = StringUtils.defaultIfBlank(fileStorageType, defaultStorageType);
        if (StringUtils.isBlank(target)) {
            target = fileStorageProperties.getDefaultType();
        }
        FileStorageTypeEnum typeEnum = FileStorageTypeEnum.fromCode(target.toUpperCase(Locale.ROOT));
        if (typeEnum != null) {
            return typeEnum;
        }
        FileStorageTypeEnum fallback = FileStorageTypeEnum.fromCode(fileStorageProperties.getDefaultType());
        return fallback == null ? FileStorageTypeEnum.LOCAL : fallback;
    }
}

