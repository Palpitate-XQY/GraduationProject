package xxqqyyy.community.infrastructure.storage;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.file.enums.FileStorageTypeEnum;

/**
 * 文件存储策略路由器。
 *
 * @author codex
 * @since 1.0.0
 */
@Component
public class FileStorageRouter {

    private final Map<FileStorageTypeEnum, FileStorageProvider> providerMap = new EnumMap<>(FileStorageTypeEnum.class);

    public FileStorageRouter(List<FileStorageProvider> providers) {
        for (FileStorageProvider provider : providers) {
            providerMap.put(provider.supportType(), provider);
        }
    }

    /**
     * 根据存储类型获取策略实现。
     *
     * @param storageType 存储类型
     * @return 策略实现
     */
    public FileStorageProvider route(FileStorageTypeEnum storageType) {
        FileStorageProvider provider = providerMap.get(storageType);
        if (provider == null) {
            throw new BizException(ErrorCode.BIZ_ERROR, "未找到对应的文件存储策略: " + storageType);
        }
        return provider;
    }
}

