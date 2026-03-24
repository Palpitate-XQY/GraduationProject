package xxqqyyy.community.modules.log.service;

import xxqqyyy.community.modules.log.entity.LogOperation;

/**
 * 操作日志服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface OperationLogService {

    void record(LogOperation operation);
}

