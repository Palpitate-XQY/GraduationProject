package xxqqyyy.community.modules.notice.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.notice.entity.BizNoticeScope;

/**
 * 公告可见范围 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface BizNoticeScopeMapper {

    int deleteByNoticeId(@Param("noticeId") Long noticeId);

    int batchInsert(@Param("scopes") Collection<BizNoticeScope> scopes);

    List<BizNoticeScope> selectByNoticeId(@Param("noticeId") Long noticeId);
}

