package xxqqyyy.community.modules.notice.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.notice.dto.NoticePageQuery;
import xxqqyyy.community.modules.notice.entity.BizNotice;

/**
 * 公告主表 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface BizNoticeMapper {

    BizNotice selectById(@Param("id") Long id);

    int insert(BizNotice notice);

    int update(BizNotice notice);

    int updateStatus(
        @Param("id") Long id,
        @Param("status") String status,
        @Param("publishTime") java.time.LocalDateTime publishTime,
        @Param("updateBy") Long updateBy
    );

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    long countManagePage(
        @Param("query") NoticePageQuery query,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds
    );

    List<BizNotice> selectManagePage(
        @Param("query") NoticePageQuery query,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds,
        @Param("offset") long offset,
        @Param("size") long size
    );

    long countResidentPage(
        @Param("query") NoticePageQuery query,
        @Param("userOrgId") Long userOrgId,
        @Param("ancestorPath") String ancestorPath
    );

    List<BizNotice> selectResidentPage(
        @Param("query") NoticePageQuery query,
        @Param("userOrgId") Long userOrgId,
        @Param("ancestorPath") String ancestorPath,
        @Param("offset") long offset,
        @Param("size") long size
    );

    long countPublished(
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds
    );
}
