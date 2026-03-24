package xxqqyyy.community.modules.log.service;

import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.log.dto.OperationLogPageQuery;
import xxqqyyy.community.modules.log.entity.LogOperation;
import xxqqyyy.community.modules.log.vo.OperationLogVO;

/**
 * 操作日志服务接口。
 *
 * @author codex
 * @since 1.0.0
 */
public interface OperationLogService {

    /**
     * 写入操作日志。
     *
     * @param operation 日志实体
     */
    void record(LogOperation operation);

    /**
     * 操作日志分页查询。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<OperationLogVO> page(OperationLogPageQuery query);
}
