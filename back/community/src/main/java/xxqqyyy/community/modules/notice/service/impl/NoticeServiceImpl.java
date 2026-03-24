package xxqqyyy.community.modules.notice.service.impl;

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
import xxqqyyy.community.modules.file.service.FileBindingService;
import xxqqyyy.community.modules.notice.dto.NoticeCreateRequest;
import xxqqyyy.community.modules.notice.dto.NoticePageQuery;
import xxqqyyy.community.modules.notice.dto.NoticeScopeItem;
import xxqqyyy.community.modules.notice.dto.NoticeUpdateRequest;
import xxqqyyy.community.modules.notice.entity.BizNotice;
import xxqqyyy.community.modules.notice.entity.BizNoticeScope;
import xxqqyyy.community.modules.notice.enums.NoticeStatusEnum;
import xxqqyyy.community.modules.notice.enums.NoticeTypeEnum;
import xxqqyyy.community.modules.notice.mapper.BizNoticeMapper;
import xxqqyyy.community.modules.notice.mapper.BizNoticeScopeMapper;
import xxqqyyy.community.modules.notice.service.NoticeService;
import xxqqyyy.community.modules.notice.vo.NoticeVO;
import xxqqyyy.community.modules.org.entity.SysOrg;
import xxqqyyy.community.modules.org.enums.OrgTypeEnum;
import xxqqyyy.community.modules.org.mapper.BizComplexPropertyRelMapper;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.system.enums.DataScopeTypeEnum;
import xxqqyyy.community.modules.system.model.DataScopeResult;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 公告服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final BizNoticeMapper bizNoticeMapper;
    private final BizNoticeScopeMapper bizNoticeScopeMapper;
    private final DataScopeService dataScopeService;
    private final SysOrgMapper sysOrgMapper;
    private final BizComplexPropertyRelMapper bizComplexPropertyRelMapper;
    private final FileBindingService fileBindingService;
    private final MarkdownRenderService markdownRenderService;

    @Override
    public PageResult<NoticeVO> managePage(NoticePageQuery query) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        DataScopeResult scope = dataScopeService.resolveByUserId(principal.getUserId());
        long total = bizNoticeMapper.countManagePage(query, scope.isAllAccess(), scope.getSafeOrgIds());
        if (total == 0) {
            return PageResult.empty(query.getCurrent(), query.getSize());
        }
        long offset = (query.getCurrent() - 1) * query.getSize();
        List<NoticeVO> records = bizNoticeMapper.selectManagePage(query, scope.isAllAccess(), scope.getSafeOrgIds(), offset, query.getSize())
            .stream().map(this::toNoticeVO).toList();
        return PageResult.<NoticeVO>builder()
            .records(records)
            .total(total)
            .current(query.getCurrent())
            .size(query.getSize())
            .build();
    }

    @Override
    public NoticeVO detail(Long noticeId) {
        BizNotice notice = requireNotice(noticeId);
        assertManageVisible(notice);
        return toNoticeVO(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(NoticeCreateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        validateScopes(request.getNoticeType(), request.getScopeItems(), principal);
        fileBindingService.assertFileIdsValid(fileBindingService.collectFileIds(request.getCoverFileId(), request.getAttachmentJson()));
        BizNotice notice = new BizNotice();
        notice.setNoticeType(request.getNoticeType());
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setCoverFileId(request.getCoverFileId());
        notice.setAttachmentJson(request.getAttachmentJson());
        notice.setTopFlag(request.getTopFlag() == null ? 0 : request.getTopFlag());
        notice.setStatus(NoticeStatusEnum.DRAFT.getCode());
        notice.setPublisherOrgId(principal.getOrgId());
        notice.setCreateBy(principal.getUserId());
        notice.setUpdateBy(principal.getUserId());
        bizNoticeMapper.insert(notice);
        fileBindingService.bindForCreate(
            "NOTICE",
            notice.getId(),
            request.getCoverFileId(),
            request.getAttachmentJson(),
            principal.getUserId()
        );
        saveScopes(notice.getId(), request.getScopeItems());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(NoticeUpdateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizNotice notice = requireNotice(request.getId());
        assertManageVisible(notice);
        validateScopes(request.getNoticeType(), request.getScopeItems(), principal);
        fileBindingService.assertFileIdsValid(fileBindingService.collectFileIds(request.getCoverFileId(), request.getAttachmentJson()));
        Long oldCoverFileId = notice.getCoverFileId();
        String oldAttachmentJson = notice.getAttachmentJson();
        notice.setNoticeType(request.getNoticeType());
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setCoverFileId(request.getCoverFileId());
        notice.setAttachmentJson(request.getAttachmentJson());
        notice.setTopFlag(request.getTopFlag() == null ? 0 : request.getTopFlag());
        notice.setUpdateBy(principal.getUserId());
        bizNoticeMapper.update(notice);
        fileBindingService.bindForUpdate(
            "NOTICE",
            notice.getId(),
            oldCoverFileId,
            oldAttachmentJson,
            request.getCoverFileId(),
            request.getAttachmentJson(),
            principal.getUserId()
        );
        saveScopes(notice.getId(), request.getScopeItems());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long noticeId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizNotice notice = requireNotice(noticeId);
        assertManageVisible(notice);
        bizNoticeMapper.logicalDelete(noticeId, principal.getUserId());
        bizNoticeScopeMapper.deleteByNoticeId(noticeId);
    }

    @Override
    public void publish(Long noticeId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizNotice notice = requireNotice(noticeId);
        assertManageVisible(notice);
        if (NoticeStatusEnum.PUBLISHED.getCode().equals(notice.getStatus())) {
            return;
        }
        bizNoticeMapper.updateStatus(noticeId, NoticeStatusEnum.PUBLISHED.getCode(), LocalDateTime.now(), principal.getUserId());
    }

    @Override
    public void recall(Long noticeId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizNotice notice = requireNotice(noticeId);
        assertManageVisible(notice);
        if (!NoticeStatusEnum.PUBLISHED.getCode().equals(notice.getStatus())) {
            throw new BizException(ErrorCode.ILLEGAL_STATE_FLOW, "仅已发布公告可撤回");
        }
        bizNoticeMapper.updateStatus(noticeId, NoticeStatusEnum.RECALLED.getCode(), notice.getPublishTime(), principal.getUserId());
    }

    @Override
    public PageResult<NoticeVO> residentPage(NoticePageQuery query) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysOrg org = requireOrg(principal.getOrgId());
        long total = bizNoticeMapper.countResidentPage(query, org.getId(), org.getAncestorPath());
        if (total == 0) {
            return PageResult.empty(query.getCurrent(), query.getSize());
        }
        long offset = (query.getCurrent() - 1) * query.getSize();
        List<NoticeVO> records = bizNoticeMapper.selectResidentPage(query, org.getId(), org.getAncestorPath(), offset, query.getSize())
            .stream().map(this::toNoticeVO).toList();
        return PageResult.<NoticeVO>builder()
            .records(records)
            .total(total)
            .current(query.getCurrent())
            .size(query.getSize())
            .build();
    }

    @Override
    public NoticeVO residentDetail(Long noticeId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizNotice notice = requireNotice(noticeId);
        if (!NoticeStatusEnum.PUBLISHED.getCode().equals(notice.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "公告不存在");
        }
        SysOrg org = requireOrg(principal.getOrgId());
        if (!isVisibleToOrg(noticeId, org.getId(), org.getAncestorPath())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该公告");
        }
        return toNoticeVO(notice);
    }

    private void saveScopes(Long noticeId, Set<NoticeScopeItem> scopeItems) {
        bizNoticeScopeMapper.deleteByNoticeId(noticeId);
        if (CollectionUtils.isEmpty(scopeItems)) {
            return;
        }
        List<BizNoticeScope> scopes = new ArrayList<>();
        for (NoticeScopeItem item : scopeItems) {
            BizNoticeScope scope = new BizNoticeScope();
            scope.setNoticeId(noticeId);
            scope.setScopeType(item.getScopeType());
            scope.setScopeRefId(item.getScopeRefId());
            scopes.add(scope);
        }
        bizNoticeScopeMapper.batchInsert(scopes);
    }

    private BizNotice requireNotice(Long noticeId) {
        BizNotice notice = bizNoticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "公告不存在");
        }
        return notice;
    }

    private SysOrg requireOrg(Long orgId) {
        SysOrg org = sysOrgMapper.selectById(orgId);
        if (org == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "组织不存在");
        }
        return org;
    }

    private void assertManageVisible(BizNotice notice) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        dataScopeService.assertOrgAccessible(principal.getUserId(), notice.getPublisherOrgId());
    }

    /**
     * 校验公告范围配置：
     * 1. 非超级管理员必须在自身数据范围内选目标组织；
     * 2. 物业公告仅允许投放到其服务的小区。
     */
    private void validateScopes(String noticeType, Set<NoticeScopeItem> scopeItems, LoginPrincipal principal) {
        if (CollectionUtils.isEmpty(scopeItems)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "公告可见范围不能为空");
        }
        Set<String> scopeTypes = new HashSet<>();
        Set<Long> scopeRefIds = new HashSet<>();
        for (NoticeScopeItem item : scopeItems) {
            scopeTypes.add(item.getScopeType());
            scopeRefIds.add(item.getScopeRefId());
            dataScopeService.assertOrgAccessible(principal.getUserId(), item.getScopeRefId());
        }
        dataScopeService.assertRoleScopeGrantable(principal.getUserId(), scopeTypes, scopeRefIds);

        if (NoticeTypeEnum.PROPERTY.getCode().equals(noticeType)) {
            for (NoticeScopeItem item : scopeItems) {
                if (!DataScopeTypeEnum.COMPLEX.getCode().equalsIgnoreCase(item.getScopeType())) {
                    throw new BizException(ErrorCode.BAD_REQUEST, "物业公告可见范围仅允许选择小区");
                }
            }
            if (!principal.isSuperAdmin()) {
                DataScopeResult scope = dataScopeService.resolveByUserId(principal.getUserId());
                List<Long> propertyCompanyIds = sysOrgMapper.selectIdsByTypeInIds(OrgTypeEnum.PROPERTY_COMPANY.getCode(), scope.getSafeOrgIds());
                if (CollectionUtils.isEmpty(propertyCompanyIds)) {
                    throw new BizException(ErrorCode.FORBIDDEN, "当前用户无物业公司范围，不能发布物业公告");
                }
                for (NoticeScopeItem item : scopeItems) {
                    boolean matched = propertyCompanyIds.stream()
                        .anyMatch(propertyId -> bizComplexPropertyRelMapper.countActiveByPropertyAndComplex(propertyId, item.getScopeRefId()) > 0);
                    if (!matched) {
                        throw new BizException(ErrorCode.FORBIDDEN, "存在未服务小区，不能发布物业公告");
                    }
                }
            }
        }
    }

    private boolean isVisibleToOrg(Long noticeId, Long orgId, String ancestorPath) {
        List<BizNoticeScope> scopes = bizNoticeScopeMapper.selectByNoticeId(noticeId);
        for (BizNoticeScope scope : scopes) {
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

    private NoticeVO toNoticeVO(BizNotice notice) {
        Set<NoticeScopeItem> scopeItems = bizNoticeScopeMapper.selectByNoticeId(notice.getId()).stream().map(scope -> {
            NoticeScopeItem item = new NoticeScopeItem();
            item.setScopeType(scope.getScopeType());
            item.setScopeRefId(scope.getScopeRefId());
            return item;
        }).collect(java.util.stream.Collectors.toSet());
        return NoticeVO.builder()
            .id(notice.getId())
            .noticeType(notice.getNoticeType())
            .title(notice.getTitle())
            .content(notice.getContent())
            .contentHtml(markdownRenderService.renderToHtml(notice.getContent()))
            .coverFileId(notice.getCoverFileId())
            .attachmentJson(notice.getAttachmentJson())
            .status(notice.getStatus())
            .topFlag(notice.getTopFlag())
            .publisherOrgId(notice.getPublisherOrgId())
            .publishTime(notice.getPublishTime())
            .createTime(notice.getCreateTime())
            .scopeItems(scopeItems)
            .build();
    }
}
