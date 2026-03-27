package xxqqyyy.community.infrastructure.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.infrastructure.storage.model.StorageConfigSnapshot;
import xxqqyyy.community.infrastructure.storage.model.StoredFileResult;
import xxqqyyy.community.modules.file.enums.FileStorageTypeEnum;

/**
 * 本地文件存储实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Slf4j
@Component
public class LocalFileStorageProvider implements FileStorageProvider {

    @Override
    public FileStorageTypeEnum supportType() {
        return FileStorageTypeEnum.LOCAL;
    }

    @Override
    public StoredFileResult store(MultipartFile file, String objectKey, StorageConfigSnapshot snapshot) throws IOException {
        Path rootPath = resolveRoot(snapshot);
        Path targetPath = rootPath.resolve(objectKey).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "非法文件路径");
        }
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return StoredFileResult.builder()
            .filePath(objectKey.replace('\\', '/'))
            .build();
    }

    @Override
    public String buildAccessUrl(String filePath, Long fileId, StorageConfigSnapshot snapshot) {
        String baseUrl = StringUtils.defaultIfBlank(snapshot.getLocalAccessBaseUrl(), "/api/files");
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + fileId + "/preview";
    }

    @Override
    public Resource loadAsResource(String filePath, StorageConfigSnapshot snapshot) throws IOException {
        Path rootPath = resolveRoot(snapshot);
        Path targetPath = rootPath.resolve(filePath).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "非法文件路径");
        }
        Resource resource = new UrlResource(targetPath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new BizException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        return resource;
    }

    @Override
    public boolean supportStreamDownload() {
        return true;
    }

    private Path resolveRoot(StorageConfigSnapshot snapshot) {
        String basePath = snapshot.getLocalBasePath();
        if (StringUtils.isBlank(basePath)) {
            throw new BizException(ErrorCode.BIZ_ERROR, "本地存储根目录未配置");
        }
        Path rootPath = Paths.get(basePath).toAbsolutePath().normalize();
        if (!Files.exists(rootPath)) {
            try {
                Files.createDirectories(rootPath);
            } catch (IOException ex) {
                log.error("创建本地存储目录失败: path={}", rootPath, ex);
                throw new BizException(ErrorCode.INTERNAL_ERROR, "创建本地存储目录失败");
            }
        }
        return rootPath;
    }
}
