package xxqqyyy.community.modules.activity.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.common.util.MarkdownRenderService;
import xxqqyyy.community.infrastructure.redis.ActivitySignupLimiter;
import xxqqyyy.community.modules.activity.dto.ActivityCreateRequest;
import xxqqyyy.community.modules.activity.dto.ActivityPageQuery;
import xxqqyyy.community.modules.activity.dto.ActivityScopeItem;
import xxqqyyy.community.modules.activity.dto.ActivityUpdateRequest;
import xxqqyyy.community.modules.activity.entity.BizActivity;
import xxqqyyy.community.modules.activity.entity.BizActivityScope;
import xxqqyyy.community.modules.activity.entity.BizActivitySignup;
import xxqqyyy.community.modules.activity.enums.ActivitySignupStatusEnum;
import xxqqyyy.community.modules.activity.enums.ActivityStatusEnum;
import xxqqyyy.community.modules.activity.mapper.BizActivityMapper;
import xxqqyyy.community.modules.activity.mapper.BizActivityScopeMapper;
import xxqqyyy.community.modules.activity.mapper.BizActivitySignupMapper;
import xxqqyyy.community.modules.activity.service.ActivityService;
import xxqqyyy.community.modules.activity.vo.ActivitySignupVO;
import xxqqyyy.community.modules.activity.vo.ActivityStatsVO;
import xxqqyyy.community.modules.activity.vo.ActivityVO;
import xxqqyyy.community.modules.file.service.FileBindingService;
import xxqqyyy.community.modules.org.entity.SysOrg;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.system.enums.DataScopeTypeEnum;
import xxqqyyy.community.modules.system.model.DataScopeResult;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 活动服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final BizActivityMapper bizActivityMapper;
    private final BizActivityScopeMapper bizActivityScopeMapper;
    private final BizActivitySignupMapper bizActivitySignupMapper;
    private final DataScopeService dataScopeService;
    private final SysOrgMapper sysOrgMapper;
    private final ActivitySignupLimiter activitySignupLimiter;
    private final FileBindingService fileBindingService;
    private final MarkdownRenderService markdownRenderService;

    @Override
    public PageResult<ActivityVO> managePage(ActivityPageQuery query) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        DataScopeResult scope = dataScopeService.resolveByUserId(principal.getUserId());
        long total = bizActivityMapper.countManagePage(query, scope.isAllAccess(), scope.getSafeOrgIds());
        if (total == 0) {
            return PageResult.empty(query.getCurrent(), query.getSize());
        }
        long offset = (query.getCurrent() - 1) * query.getSize();
        List<ActivityVO> records = bizActivityMapper.selectManagePage(query, scope.isAllAccess(), scope.getSafeOrgIds(), offset, query.getSize())
            .stream().map(this::toActivityVO).toList();
        return PageResult.<ActivityVO>builder()
            .records(records)
            .total(total)
            .current(query.getCurrent())
            .size(query.getSize())
            .build();
    }

    @Override
    public ActivityVO detail(Long activityId) {
        BizActivity activity = requireActivity(activityId);
        assertManageVisible(activity);
        return toActivityVO(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ActivityCreateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        validateTimeRange(request.getSignupStartTime(), request.getSignupEndTime(), request.getActivityStartTime(), request.getActivityEndTime());
        validateScopes(request.getScopeItems(), principal);
        fileBindingService.assertFileIdsValid(fileBindingService.collectFileIds(request.getCoverFileId(), request.getAttachmentJson()));
        BizActivity activity = new BizActivity();
        activity.setTitle(request.getTitle());
        activity.setContent(request.getContent());
        activity.setCoverFileId(request.getCoverFileId());
        activity.setAttachmentJson(request.getAttachmentJson());
        activity.setActivityStartTime(request.getActivityStartTime());
        activity.setActivityEndTime(request.getActivityEndTime());
        activity.setSignupStartTime(request.getSignupStartTime());
        activity.setSignupEndTime(request.getSignupEndTime());
        activity.setLocation(request.getLocation());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setStatus(ActivityStatusEnum.DRAFT.getCode());
        activity.setPublisherOrgId(principal.getOrgId());
        activity.setCreateBy(principal.getUserId());
        activity.setUpdateBy(principal.getUserId());
        bizActivityMapper.insert(activity);
        fileBindingService.bindForCreate(
            "ACTIVITY",
            activity.getId(),
            request.getCoverFileId(),
            request.getAttachmentJson(),
            principal.getUserId()
        );
        saveScopes(activity.getId(), request.getScopeItems());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActivityUpdateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizActivity activity = requireActivity(request.getId());
        assertManageVisible(activity);
        validateTimeRange(request.getSignupStartTime(), request.getSignupEndTime(), request.getActivityStartTime(), request.getActivityEndTime());
        validateScopes(request.getScopeItems(), principal);
        fileBindingService.assertFileIdsValid(fileBindingService.collectFileIds(request.getCoverFileId(), request.getAttachmentJson()));
        Long oldCoverFileId = activity.getCoverFileId();
        String oldAttachmentJson = activity.getAttachmentJson();
        activity.setTitle(request.getTitle());
        activity.setContent(request.getContent());
        activity.setCoverFileId(request.getCoverFileId());
        activity.setAttachmentJson(request.getAttachmentJson());
        activity.setActivityStartTime(request.getActivityStartTime());
        activity.setActivityEndTime(request.getActivityEndTime());
        activity.setSignupStartTime(request.getSignupStartTime());
        activity.setSignupEndTime(request.getSignupEndTime());
        activity.setLocation(request.getLocation());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setUpdateBy(principal.getUserId());
        bizActivityMapper.update(activity);
        fileBindingService.bindForUpdate(
            "ACTIVITY",
            activity.getId(),
            oldCoverFileId,
            oldAttachmentJson,
            request.getCoverFileId(),
            request.getAttachmentJson(),
            principal.getUserId()
        );
        saveScopes(activity.getId(), request.getScopeItems());
        activitySignupLimiter.evict(activity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long activityId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizActivity activity = requireActivity(activityId);
        assertManageVisible(activity);
        bizActivityMapper.logicalDelete(activityId, principal.getUserId());
        bizActivityScopeMapper.deleteByActivityId(activityId);
        activitySignupLimiter.evict(activityId);
    }

    @Override
    public void publish(Long activityId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizActivity activity = requireActivity(activityId);
        assertManageVisible(activity);
        bizActivityMapper.updateStatus(activityId, ActivityStatusEnum.PUBLISHED.getCode(), LocalDateTime.now(), principal.getUserId());
        activitySignupLimiter.evict(activityId);
    }

    @Override
    public void recall(Long activityId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizActivity activity = requireActivity(activityId);
        assertManageVisible(activity);
        if (!ActivityStatusEnum.PUBLISHED.getCode().equals(activity.getStatus())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE_FLOW, "仅已发布活动可撤回");
        }
        bizActivityMapper.updateStatus(activityId, ActivityStatusEnum.RECALLED.getCode(), activity.getPublishTime(), principal.getUserId());
        activitySignupLimiter.evict(activityId);
    }

    @Override
    public PageResult<ActivityVO> residentPage(ActivityPageQuery query) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysOrg org = requireOrg(principal.getOrgId());
        long total = bizActivityMapper.countResidentPage(query, org.getId(), org.getAncestorPath());
        if (total == 0) {
            return PageResult.empty(query.getCurrent(), query.getSize());
        }
        long offset = (query.getCurrent() - 1) * query.getSize();
        List<ActivityVO> records = bizActivityMapper.selectResidentPage(query, org.getId(), org.getAncestorPath(), offset, query.getSize())
            .stream().map(this::toActivityVO).toList();
        return PageResult.<ActivityVO>builder()
            .records(records)
            .total(total)
            .current(query.getCurrent())
            .size(query.getSize())
            .build();
    }

    @Override
    public ActivityVO residentDetail(Long activityId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizActivity activity = requireActivity(activityId);
        if (!ActivityStatusEnum.PUBLISHED.getCode().equals(activity.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        SysOrg org = requireOrg(principal.getOrgId());
        if (!isVisibleToOrg(activityId, org.getId(), org.getAncestorPath())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看活动");
        }
        return toActivityVO(activity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signup(Long activityId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizActivity activity = requireActivity(activityId);
        assertSignupAllowed(activity);
        SysOrg org = requireOrg(principal.getOrgId());
        if (!isVisibleToOrg(activityId, org.getId(), org.getAncestorPath())) {
            throw new BizException(ErrorCode.FORBIDDEN, "活动不可见，不能报名");
        }
        BizActivitySignup existing = bizActivitySignupMapper.selectByActivityAndUser(activityId, principal.getUserId());
        if (existing != null && ActivitySignupStatusEnum.SIGNED.getCode().equals(existing.getSignupStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "请勿重复报名");
        }
        long currentCount = bizActivitySignupMapper.countSignedByActivity(activityId);
        if (currentCount >= activity.getMaxParticipants()) {
            throw new BizException(ErrorCode.CONFLICT, "活动名额已满");
        }
        boolean reserved = activitySignupLimiter.reserve(activityId, currentCount, activity.getMaxParticipants());
        if (!reserved) {
            throw new BizException(ErrorCode.CONFLICT, "活动名额已满");
        }
        try {
            if (existing == null) {
                BizActivitySignup signup = new BizActivitySignup();
                signup.setActivityId(activityId);
                signup.setUserId(principal.getUserId());
                signup.setSignupStatus(ActivitySignupStatusEnum.SIGNED.getCode());
                signup.setSignupTime(LocalDateTime.now());
                signup.setCreateBy(principal.getUserId());
                signup.setUpdateBy(principal.getUserId());
                bizActivitySignupMapper.insert(signup);
            } else {
                bizActivitySignupMapper.updateStatus(existing.getId(), ActivitySignupStatusEnum.SIGNED.getCode(), null, principal.getUserId());
            }
        } catch (Exception ex) {
            activitySignupLimiter.release(activityId, currentCount + 1);
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSignup(Long activityId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizActivitySignup signup = bizActivitySignupMapper.selectByActivityAndUser(activityId, principal.getUserId());
        if (signup == null || !ActivitySignupStatusEnum.SIGNED.getCode().equals(signup.getSignupStatus())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "当前用户未报名，无法取消");
        }
        long currentCount = bizActivitySignupMapper.countSignedByActivity(activityId);
        bizActivitySignupMapper.updateStatus(signup.getId(), ActivitySignupStatusEnum.CANCELED.getCode(), LocalDateTime.now(), principal.getUserId());
        activitySignupLimiter.release(activityId, currentCount);
    }

    @Override
    public List<ActivitySignupVO> signupList(Long activityId) {
        BizActivity activity = requireActivity(activityId);
        assertManageVisible(activity);
        return bizActivitySignupMapper.selectSignedListByActivity(activityId).stream().map(signup -> ActivitySignupVO.builder()
            .id(signup.getId())
            .activityId(signup.getActivityId())
            .userId(signup.getUserId())
            .signupStatus(signup.getSignupStatus())
            .signupTime(signup.getSignupTime())
            .cancelTime(signup.getCancelTime())
            .build()).toList();
    }

    @Override
    public ActivityStatsVO stats(Long activityId) {
        BizActivity activity = requireActivity(activityId);
        assertManageVisible(activity);
        long signupCount = bizActivitySignupMapper.countSignedByActivity(activityId);
        return ActivityStatsVO.builder()
            .activityId(activityId)
            .signupCount(signupCount)
            .maxParticipants(activity.getMaxParticipants())
            .remainingSlots(Math.max(0, activity.getMaxParticipants() - signupCount))
            .build();
    }

    private void saveScopes(Long activityId, Set<ActivityScopeItem> scopeItems) {
        bizActivityScopeMapper.deleteByActivityId(activityId);
        List<BizActivityScope> scopes = new ArrayList<>();
        for (ActivityScopeItem item : scopeItems) {
            BizActivityScope scope = new BizActivityScope();
            scope.setActivityId(activityId);
            scope.setScopeType(item.getScopeType());
            scope.setScopeRefId(item.getScopeRefId());
            scopes.add(scope);
        }
        bizActivityScopeMapper.batchInsert(scopes);
    }

    private void validateScopes(Set<ActivityScopeItem> scopeItems, LoginPrincipal principal) {
        if (CollectionUtils.isEmpty(scopeItems)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "活动可见范围不能为空");
        }
        Set<String> scopeTypes = new HashSet<>();
        Set<Long> scopeIds = new HashSet<>();
        for (ActivityScopeItem item : scopeItems) {
            if (DataScopeTypeEnum.SELF.getCode().equalsIgnoreCase(item.getScopeType())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "活动范围不支持 SELF 类型");
            }
            scopeTypes.add(item.getScopeType());
            scopeIds.add(item.getScopeRefId());
            dataScopeService.assertOrgAccessible(principal.getUserId(), item.getScopeRefId());
        }
        dataScopeService.assertRoleScopeGrantable(principal.getUserId(), scopeTypes, scopeIds);
    }

    private void validateTimeRange(
        LocalDateTime signupStartTime,
        LocalDateTime signupEndTime,
        LocalDateTime activityStartTime,
        LocalDateTime activityEndTime
    ) {
        if (signupStartTime.isAfter(signupEndTime)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "报名开始时间不能晚于报名结束时间");
        }
        if (activityStartTime.isAfter(activityEndTime)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "活动开始时间不能晚于活动结束时间");
        }
        if (signupEndTime.isAfter(activityEndTime)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "报名结束时间不能晚于活动结束时间");
        }
    }

    private void assertSignupAllowed(BizActivity activity) {
        if (!ActivityStatusEnum.PUBLISHED.getCode().equals(activity.getStatus())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "活动未发布，不能报名");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getSignupStartTime()) || now.isAfter(activity.getSignupEndTime())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "当前不在报名时间范围内");
        }
    }

    private void assertManageVisible(BizActivity activity) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        dataScopeService.assertOrgAccessible(principal.getUserId(), activity.getPublisherOrgId());
    }

    private BizActivity requireActivity(Long activityId) {
        BizActivity activity = bizActivityMapper.selectById(activityId);
        if (activity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        return activity;
    }

    private SysOrg requireOrg(Long orgId) {
        SysOrg org = sysOrgMapper.selectById(orgId);
        if (org == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "组织不存在");
        }
        return org;
    }

    private boolean isVisibleToOrg(Long activityId, Long orgId, String ancestorPath) {
        List<BizActivityScope> scopes = bizActivityScopeMapper.selectByActivityId(activityId);
        for (BizActivityScope scope : scopes) {
            if ("ALL".equalsIgnoreCase(scope.getScopeType())) {
                return true;
            }
            if (scope.getScopeRefId() == null) {
                continue;
            }
            if (scope.getScopeRefId().equals(orgId)) {
                return true;
            }
            if (ancestorPath != null && ancestorPath.contains("/" + scope.getScopeRefId() + "/")) {
                return true;
            }
        }
        return false;
    }

    private ActivityVO toActivityVO(BizActivity activity) {
        Set<ActivityScopeItem> scopeItems = bizActivityScopeMapper.selectByActivityId(activity.getId()).stream().map(scope -> {
            ActivityScopeItem item = new ActivityScopeItem();
            item.setScopeType(scope.getScopeType());
            item.setScopeRefId(scope.getScopeRefId());
            return item;
        }).collect(java.util.stream.Collectors.toSet());
        return ActivityVO.builder()
            .id(activity.getId())
            .title(activity.getTitle())
            .content(activity.getContent())
            .contentHtml(markdownRenderService.renderToHtml(activity.getContent()))
            .coverFileId(activity.getCoverFileId())
            .attachmentJson(activity.getAttachmentJson())
            .activityStartTime(activity.getActivityStartTime())
            .activityEndTime(activity.getActivityEndTime())
            .signupStartTime(activity.getSignupStartTime())
            .signupEndTime(activity.getSignupEndTime())
            .location(activity.getLocation())
            .maxParticipants(activity.getMaxParticipants())
            .status(activity.getStatus())
            .publisherOrgId(activity.getPublisherOrgId())
            .publishTime(activity.getPublishTime())
            .createTime(activity.getCreateTime())
            .scopeItems(scopeItems)
            .build();
    }
}
