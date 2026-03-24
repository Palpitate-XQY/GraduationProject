package xxqqyyy.community.modules.log.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xxqqyyy.community.modules.log.entity.LogOperation;
import xxqqyyy.community.modules.log.mapper.LogOperationMapper;
import xxqqyyy.community.modules.log.service.OperationLogService;

/**
 * 操作日志服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final LogOperationMapper logOperationMapper;

    @Override
    public void record(LogOperation operation) {
        logOperationMapper.insert(operation);
    }
}

