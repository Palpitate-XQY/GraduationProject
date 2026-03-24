package xxqqyyy.community.modules.repair.mapper;

import java.util.List;
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
}
