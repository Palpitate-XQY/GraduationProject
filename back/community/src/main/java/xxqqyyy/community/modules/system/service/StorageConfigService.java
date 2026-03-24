package xxqqyyy.community.modules.system.service;

import xxqqyyy.community.infrastructure.storage.model.StorageConfigSnapshot;
import xxqqyyy.community.modules.system.dto.StorageConfigUpdateRequest;
import xxqqyyy.community.modules.system.vo.StorageConfigVO;

/**
 * 文件存储配置服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface StorageConfigService {

    /**
     * 获取当前配置（仅超级管理员）。
     *
     * @return 存储配置
     */
    StorageConfigVO currentForAdmin();

    /**
     * 更新存储配置（仅超级管理员）。
     *
     * @param request 更新请求
     */
    void update(StorageConfigUpdateRequest request);

    /**
     * 获取运行时快照（业务服务内部调用）。
     *
     * @return 配置快照
     */
    StorageConfigSnapshot currentSnapshot();
}

