package xxqqyyy.community.modules.log.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.log.dto.OperationLogPageQuery;
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

    long countPage(@Param("query") OperationLogPageQuery query);

    List<LogOperation> selectPage(
        @Param("query") OperationLogPageQuery query,
        @Param("offset") long offset,
        @Param("size") long size
    );
}
