package xxqqyyy.community.modules.activity.service;

import java.util.List;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.activity.dto.ActivityCreateRequest;
import xxqqyyy.community.modules.activity.dto.ActivityPageQuery;
import xxqqyyy.community.modules.activity.dto.ActivityUpdateRequest;
import xxqqyyy.community.modules.activity.vo.ActivitySignupVO;
import xxqqyyy.community.modules.activity.vo.ActivityStatsVO;
import xxqqyyy.community.modules.activity.vo.ActivityVO;

/**
 * 活动服务接口。
 *
 * @author codex
 * @since 1.0.0
 */
public interface ActivityService {

    PageResult<ActivityVO> managePage(ActivityPageQuery query);

    ActivityVO detail(Long activityId);

    void create(ActivityCreateRequest request);

    void update(ActivityUpdateRequest request);

    void delete(Long activityId);

    void publish(Long activityId);

    void recall(Long activityId);

    PageResult<ActivityVO> residentPage(ActivityPageQuery query);

    PageResult<ActivityVO> myPage(ActivityPageQuery query);

    ActivityVO residentDetail(Long activityId);

    void signup(Long activityId);

    void cancelSignup(Long activityId);

    List<ActivitySignupVO> signupList(Long activityId);

    ActivityStatsVO stats(Long activityId);
}
