package xxqqyyy.community.modules.system.service.impl;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.infrastructure.storage.FileStorageProperties;
import xxqqyyy.community.infrastructure.storage.model.StorageConfigSnapshot;
import xxqqyyy.community.modules.file.enums.FileStorageTypeEnum;
import xxqqyyy.community.modules.system.dto.StorageConfigUpdateRequest;
import xxqqyyy.community.modules.system.entity.SysStorageConfig;
import xxqqyyy.community.modules.system.mapper.SysStorageConfigMapper;
import xxqqyyy.community.modules.system.service.StorageConfigService;
import xxqqyyy.community.modules.system.vo.StorageConfigVO;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 文件存储配置服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class StorageConfigServiceImpl implements StorageConfigService {

    private final SysStorageConfigMapper sysStorageConfigMapper;
    private final FileStorageProperties fileStorageProperties;

    @Override
    public StorageConfigVO currentForAdmin() {
        assertSuperAdmin();
        SysStorageConfig config = ensureCurrentConfig(null);
        return toVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(StorageConfigUpdateRequest request) {
        LoginPrincipal principal = assertSuperAdmin();
        String storageType = normalizeStorageType(request.getStorageType());
        SysStorageConfig current = ensureCurrentConfig(principal.getUserId());
        String accessKey = resolveValue(current.getQiniuAccessKey(), request.getQiniuAccessKey());
        String secretKey = resolveSecretKey(current, request);
        String bucket = resolveValue(current.getQiniuBucket(), request.getQiniuBucket());
        String region = resolveRegion(current.getQiniuRegion(), request.getQiniuRegion());
        String domain = resolveValue(current.getQiniuDomain(), request.getQiniuDomain());

        if (FileStorageTypeEnum.QINIU.getCode().equalsIgnoreCase(storageType)) {
            if (StringUtils.isAnyBlank(
                accessKey,
                secretKey,
                bucket,
                domain)
            ) {
                throw new BizException(ErrorCode.BAD_REQUEST, "启用七牛存储时必须填写 accessKey/secretKey/bucket/domain");
            }
        }

        current.setStorageType(storageType);
        current.setLocalBasePath(StringUtils.defaultIfBlank(request.getLocalBasePath(), current.getLocalBasePath()));
        current.setLocalAccessBaseUrl(StringUtils.defaultIfBlank(request.getLocalAccessBaseUrl(), current.getLocalAccessBaseUrl()));
        current.setQiniuAccessKey(accessKey);
        current.setQiniuSecretKey(secretKey);
        current.setQiniuBucket(bucket);
        current.setQiniuRegion(region);
        current.setQiniuDomain(domain);
        current.setRemark(StringUtils.trimToNull(request.getRemark()));
        current.setUpdateBy(principal.getUserId());
        sysStorageConfigMapper.update(current);
    }

    @Override
    public StorageConfigSnapshot currentSnapshot() {
        SysStorageConfig config = ensureCurrentConfig(null);
        return StorageConfigSnapshot.builder()
            .storageType(config.getStorageType())
            .localBasePath(config.getLocalBasePath())
            .localAccessBaseUrl(config.getLocalAccessBaseUrl())
            .qiniuAccessKey(config.getQiniuAccessKey())
            .qiniuSecretKey(config.getQiniuSecretKey())
            .qiniuBucket(config.getQiniuBucket())
            .qiniuRegion(config.getQiniuRegion())
            .qiniuDomain(config.getQiniuDomain())
            .build();
    }

    private SysStorageConfig ensureCurrentConfig(Long operatorUserId) {
        SysStorageConfig config = sysStorageConfigMapper.selectCurrent();
        if (config != null) {
            if (StringUtils.isBlank(config.getStorageType())) {
                config.setStorageType(normalizeStorageType(fileStorageProperties.getDefaultType()));
            }
            if (StringUtils.isBlank(config.getLocalBasePath())) {
                config.setLocalBasePath(fileStorageProperties.getLocalBasePath());
            }
            if (StringUtils.isBlank(config.getLocalAccessBaseUrl())) {
                config.setLocalAccessBaseUrl(fileStorageProperties.getLocalAccessBaseUrl());
            }
            if (StringUtils.isBlank(config.getQiniuRegion())) {
                config.setQiniuRegion("auto");
            }
            return config;
        }
        SysStorageConfig initConfig = new SysStorageConfig();
        initConfig.setStorageType(normalizeStorageType(fileStorageProperties.getDefaultType()));
        initConfig.setLocalBasePath(fileStorageProperties.getLocalBasePath());
        initConfig.setLocalAccessBaseUrl(fileStorageProperties.getLocalAccessBaseUrl());
        initConfig.setQiniuRegion("auto");
        initConfig.setCreateBy(operatorUserId);
        initConfig.setUpdateBy(operatorUserId);
        sysStorageConfigMapper.insert(initConfig);
        return initConfig;
    }

    private StorageConfigVO toVO(SysStorageConfig config) {
        return StorageConfigVO.builder()
            .storageType(config.getStorageType())
            .localBasePath(config.getLocalBasePath())
            .localAccessBaseUrl(config.getLocalAccessBaseUrl())
            .qiniuAccessKey(config.getQiniuAccessKey())
            .qiniuSecretKeyMasked(maskSecret(config.getQiniuSecretKey()))
            .qiniuBucket(config.getQiniuBucket())
            .qiniuRegion(config.getQiniuRegion())
            .qiniuDomain(config.getQiniuDomain())
            .remark(config.getRemark())
            .build();
    }

    private String maskSecret(String secret) {
        if (StringUtils.isBlank(secret)) {
            return null;
        }
        if (secret.length() <= 8) {
            return "********";
        }
        return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
    }

    private String resolveSecretKey(SysStorageConfig current, StorageConfigUpdateRequest request) {
        if (StringUtils.isNotBlank(request.getQiniuSecretKey())) {
            return request.getQiniuSecretKey().trim();
        }
        return current.getQiniuSecretKey();
    }

    private String resolveValue(String currentValue, String requestValue) {
        if (StringUtils.isBlank(requestValue)) {
            return StringUtils.trimToNull(currentValue);
        }
        return requestValue.trim();
    }

    private String resolveRegion(String currentRegion, String requestRegion) {
        if (StringUtils.isBlank(requestRegion)) {
            if (StringUtils.isBlank(currentRegion)) {
                return "auto";
            }
            return currentRegion.toLowerCase(Locale.ROOT);
        }
        return requestRegion.toLowerCase(Locale.ROOT);
    }

    private String normalizeStorageType(String storageType) {
        String target = StringUtils.defaultIfBlank(storageType, FileStorageTypeEnum.LOCAL.getCode()).toUpperCase(Locale.ROOT);
        if (!FileStorageTypeEnum.valid(target)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "不支持的存储类型: " + storageType);
        }
        return target;
    }

    private LoginPrincipal assertSuperAdmin() {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        if (!principal.isSuperAdmin()) {
            throw new BizException(ErrorCode.FORBIDDEN, "仅超级管理员可操作存储配置");
        }
        return principal;
    }
}
