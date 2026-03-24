package xxqqyyy.community.modules.repair.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.repair.entity.BizRepairAttachment;

/**
 * 报修附件 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface BizRepairAttachmentMapper {

    int batchInsert(@Param("list") List<BizRepairAttachment> list);

    List<BizRepairAttachment> selectByRepairOrderId(@Param("repairOrderId") Long repairOrderId);
}
