package xxqqyyy.community.modules.repair.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.repair.dto.RepairPageQuery;
import xxqqyyy.community.modules.repair.entity.BizRepairOrder;

/**
 * 报修工单 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface BizRepairOrderMapper {

    BizRepairOrder selectById(@Param("id") Long id);

    int insert(BizRepairOrder order);

    int updateSelective(BizRepairOrder order);

    long countMyPage(@Param("query") RepairPageQuery query, @Param("residentUserId") Long residentUserId);

    List<BizRepairOrder> selectMyPage(
        @Param("query") RepairPageQuery query,
        @Param("residentUserId") Long residentUserId,
        @Param("offset") long offset,
        @Param("size") long size
    );

    long countManagePage(
        @Param("query") RepairPageQuery query,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds
    );

    List<BizRepairOrder> selectManagePage(
        @Param("query") RepairPageQuery query,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds,
        @Param("offset") long offset,
        @Param("size") long size
    );
}
