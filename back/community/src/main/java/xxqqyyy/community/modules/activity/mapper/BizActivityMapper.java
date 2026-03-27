package xxqqyyy.community.modules.activity.mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.activity.dto.ActivityPageQuery;
import xxqqyyy.community.modules.activity.entity.BizActivity;

/**
 * 活动主表 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface BizActivityMapper {

    BizActivity selectById(@Param("id") Long id);

    int insert(BizActivity activity);

    int update(BizActivity activity);

    int updateStatus(
        @Param("id") Long id,
        @Param("status") String status,
        @Param("publishTime") LocalDateTime publishTime,
        @Param("updateBy") Long updateBy
    );

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    long countManagePage(
        @Param("query") ActivityPageQuery query,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds
    );

    List<BizActivity> selectManagePage(
        @Param("query") ActivityPageQuery query,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds,
        @Param("offset") long offset,
        @Param("size") long size
    );

    long countResidentPage(
        @Param("query") ActivityPageQuery query,
        @Param("userOrgId") Long userOrgId,
        @Param("ancestorPath") String ancestorPath
    );

    List<BizActivity> selectResidentPage(
        @Param("query") ActivityPageQuery query,
        @Param("userOrgId") Long userOrgId,
        @Param("ancestorPath") String ancestorPath,
        @Param("offset") long offset,
        @Param("size") long size
    );

    long countMyPage(
        @Param("query") ActivityPageQuery query,
        @Param("userId") Long userId
    );

    List<BizActivity> selectMyPage(
        @Param("query") ActivityPageQuery query,
        @Param("userId") Long userId,
        @Param("offset") long offset,
        @Param("size") long size
    );

    long countPublished(
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds
    );

    long countPublishedInPeriod(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds
    );

    long countSignupInPeriod(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds
    );

    List<String> selectTextCorpusInPeriod(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds,
        @Param("limit") int limit
    );
}
