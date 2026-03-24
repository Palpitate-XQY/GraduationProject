package xxqqyyy.community.infrastructure.storage;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import java.io.IOException;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.infrastructure.storage.model.StorageConfigSnapshot;
import xxqqyyy.community.infrastructure.storage.model.StoredFileResult;
import xxqqyyy.community.modules.file.enums.FileStorageTypeEnum;

/**
 * 七牛云文件存储实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Slf4j
@Component
public class QiniuFileStorageProvider implements FileStorageProvider {

    @Override
    public FileStorageTypeEnum supportType() {
        return FileStorageTypeEnum.QINIU;
    }

    @Override
    public StoredFileResult store(MultipartFile file, String objectKey, StorageConfigSnapshot snapshot) throws IOException {
        validateQiniuConfig(snapshot);
        UploadManager uploadManager = new UploadManager(createConfiguration(snapshot.getQiniuRegion()));
        Auth auth = Auth.create(snapshot.getQiniuAccessKey(), snapshot.getQiniuSecretKey());
        String uploadToken = auth.uploadToken(snapshot.getQiniuBucket());
        Response response = null;
        try {
            response = uploadManager.put(file.getBytes(), objectKey, uploadToken);
            if (response.statusCode / 100 != 2) {
                String body = response.bodyString();
                log.error("七牛上传失败: code={}, body={}", response.statusCode, body);
                throw new BizException(ErrorCode.BIZ_ERROR, "七牛上传失败: " + body);
            }
            return StoredFileResult.builder()
                .filePath(objectKey)
                .build();
        } catch (QiniuException ex) {
            String error = ex.response == null ? ex.getMessage() : ex.response.toString();
            log.error("七牛上传异常: {}", error, ex);
            throw new BizException(ErrorCode.BIZ_ERROR, "七牛上传异常: " + error);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    public String buildAccessUrl(String filePath, Long fileId, StorageConfigSnapshot snapshot) {
        String domain = StringUtils.trimToEmpty(snapshot.getQiniuDomain());
        if (StringUtils.isBlank(domain)) {
            return filePath;
        }
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            domain = "https://" + domain;
        }
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        return domain + "/" + filePath;
    }

    private void validateQiniuConfig(StorageConfigSnapshot snapshot) {
        if (StringUtils.isAnyBlank(
            snapshot.getQiniuAccessKey(),
            snapshot.getQiniuSecretKey(),
            snapshot.getQiniuBucket(),
            snapshot.getQiniuDomain())
        ) {
            throw new BizException(ErrorCode.BIZ_ERROR, "七牛存储参数不完整，请先配置 accessKey/secretKey/bucket/domain");
        }
    }

    private Configuration createConfiguration(String regionCode) {
        String code = StringUtils.defaultIfBlank(regionCode, "auto").toLowerCase(Locale.ROOT);
        Region region = switch (code) {
            case "z0", "region0", "huadong" -> Region.region0();
            case "z1", "region1", "huabei" -> Region.region1();
            case "z2", "region2", "huanan" -> Region.region2();
            case "na0", "regionna0", "beimei" -> Region.regionNa0();
            case "as0", "regionas0", "xinjiapo" -> Region.regionAs0();
            default -> Region.autoRegion();
        };
        return Configuration.create(region);
    }
}

