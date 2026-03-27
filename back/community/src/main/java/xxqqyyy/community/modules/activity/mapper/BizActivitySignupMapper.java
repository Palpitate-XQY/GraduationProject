package xxqqyyy.community.modules.activity.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.activity.entity.BizActivitySignup;

/**
 * 活动报名 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface BizActivitySignupMapper {

    BizActivitySignup selectByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);

    int insert(BizActivitySignup signup);

    int updateStatus(
        @Param("id") Long id,
        @Param("signupStatus") String signupStatus,
        @Param("cancelTime") java.time.LocalDateTime cancelTime,
        @Param("updateBy") Long updateBy
    );

    long countSignedByActivity(@Param("activityId") Long activityId);

    List<BizActivitySignup> selectSignedListByActivity(@Param("activityId") Long activityId);

    List<BizActivitySignup> selectByUserAndActivityIds(
        @Param("userId") Long userId,
        @Param("activityIds") List<Long> activityIds
    );
}
