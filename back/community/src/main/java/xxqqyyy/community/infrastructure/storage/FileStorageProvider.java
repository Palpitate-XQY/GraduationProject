package xxqqyyy.community.infrastructure.storage;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import xxqqyyy.community.infrastructure.storage.model.StorageConfigSnapshot;
import xxqqyyy.community.infrastructure.storage.model.StoredFileResult;
import xxqqyyy.community.modules.file.enums.FileStorageTypeEnum;

/**
 * 文件存储策略接口。
 *
 * @author codex
 * @since 1.0.0
 */
public interface FileStorageProvider {

    /**
     * 当前策略支持的存储类型。
     *
     * @return 存储类型
     */
    FileStorageTypeEnum supportType();

    /**
     * 执行文件上传。
     *
     * @param file 文件
     * @param objectKey 对象路径
     * @param snapshot 存储配置
     * @return 上传结果
     * @throws IOException 上传异常
     */
    StoredFileResult store(MultipartFile file, String objectKey, StorageConfigSnapshot snapshot) throws IOException;

    /**
     * 构建访问地址。
     *
     * @param filePath 文件路径
     * @param fileId 文件ID
     * @param snapshot 存储配置
     * @return 可访问地址
     */
    String buildAccessUrl(String filePath, Long fileId, StorageConfigSnapshot snapshot);

    /**
     * 加载文件资源（仅本地存储需要）。
     *
     * @param filePath 文件路径
     * @param snapshot 存储配置
     * @return 资源对象
     * @throws IOException 读取异常
     */
    default Resource loadAsResource(String filePath, StorageConfigSnapshot snapshot) throws IOException {
        return null;
    }

    /**
     * 是否支持服务端直出下载。
     *
     * @return true 表示支持
     */
    default boolean supportStreamDownload() {
        return false;
    }
}

