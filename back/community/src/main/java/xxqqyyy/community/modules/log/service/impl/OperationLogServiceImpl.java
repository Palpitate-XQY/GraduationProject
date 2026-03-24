package xxqqyyy.community.modules.log.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.log.dto.OperationLogPageQuery;
import xxqqyyy.community.modules.log.entity.LogOperation;
import xxqqyyy.community.modules.log.mapper.LogOperationMapper;
import xxqqyyy.community.modules.log.service.OperationLogService;
import xxqqyyy.community.modules.log.vo.OperationLogVO;

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

    @Override
    public PageResult<OperationLogVO> page(OperationLogPageQuery query) {
        long total = logOperationMapper.countPage(query);
        if (total == 0) {
            return PageResult.empty(query.getCurrent(), query.getSize());
        }
        long offset = (query.getCurrent() - 1) * query.getSize();
        var records = logOperationMapper.selectPage(query, offset, query.getSize()).stream().map(log -> OperationLogVO.builder()
            .id(log.getId())
            .userId(log.getUserId())
            .username(log.getUsername())
            .operationModule(log.getOperationModule())
            .operationType(log.getOperationType())
            .requestUri(log.getRequestUri())
            .requestMethod(log.getRequestMethod())
            .successFlag(log.getSuccessFlag())
            .errorMessage(log.getErrorMessage())
            .traceId(log.getTraceId())
            .operationTime(log.getOperationTime())
            .build()).toList();
        return PageResult.<OperationLogVO>builder()
            .records(records)
            .total(total)
            .current(query.getCurrent())
            .size(query.getSize())
            .build();
    }
}
