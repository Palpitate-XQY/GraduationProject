package xxqqyyy.community.modules.repair.mapper;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Collection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.repair.entity.BizRepairOrderLog;

/**
 * 报修工单流转日志 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface BizRepairOrderLogMapper {

    int insert(BizRepairOrderLog log);

    List<BizRepairOrderLog> selectByRepairOrderId(@Param("repairOrderId") Long repairOrderId);

    long countOperationInPeriod(
        @Param("operationType") String operationType,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds
    );
}
