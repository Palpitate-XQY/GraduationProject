package xxqqyyy.community.modules.log.mapper;

import org.apache.ibatis.annotations.Mapper;
import xxqqyyy.community.modules.log.entity.LogOperation;

/**
 * 操作日志 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface LogOperationMapper {

    int insert(LogOperation operation);
}

