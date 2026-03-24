package xxqqyyy.community.modules.activity.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.activity.entity.BizActivityScope;

/**
 * 活动可见范围 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface BizActivityScopeMapper {

    int deleteByActivityId(@Param("activityId") Long activityId);

    int batchInsert(@Param("scopes") Collection<BizActivityScope> scopes);

    List<BizActivityScope> selectByActivityId(@Param("activityId") Long activityId);
}

