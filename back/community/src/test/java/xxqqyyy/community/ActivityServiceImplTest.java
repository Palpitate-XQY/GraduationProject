package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import xxqqyyy.community.common.util.MarkdownRenderService;
import xxqqyyy.community.infrastructure.redis.ActivitySignupLimiter;
import xxqqyyy.community.modules.activity.dto.ActivityScopeItem;
import xxqqyyy.community.modules.activity.dto.ActivityUpdateRequest;
import xxqqyyy.community.modules.activity.entity.BizActivity;
import xxqqyyy.community.modules.activity.entity.BizActivityScope;
import xxqqyyy.community.modules.activity.mapper.BizActivityMapper;
import xxqqyyy.community.modules.activity.mapper.BizActivityScopeMapper;
import xxqqyyy.community.modules.activity.mapper.BizActivitySignupMapper;
import xxqqyyy.community.modules.activity.service.impl.ActivityServiceImpl;
import xxqqyyy.community.modules.activity.vo.ActivityVO;
import xxqqyyy.community.modules.file.service.FileAttachmentService;
import xxqqyyy.community.modules.file.service.FileBindingService;
import xxqqyyy.community.modules.file.vo.FileAttachmentVO;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 活动服务测试，覆盖 Markdown 渲染与附件绑定行为。
 *
 * @author codex
 * @since 1.0.0
 */
class ActivityServiceImplTest {

    private BizActivityMapper bizActivityMapper;
    private BizActivityScopeMapper bizActivityScopeMapper;
    private BizActivitySignupMapper bizActivitySignupMapper;
    private DataScopeService dataScopeService;
    private SysOrgMapper sysOrgMapper;
    private ActivitySignupLimiter activitySignupLimiter;
    private FileBindingService fileBindingService;
    private FileAttachmentService fileAttachmentService;
    private MarkdownRenderService markdownRenderService;
    private ActivityServiceImpl activityService;

    @BeforeEach
    void setUp() {
        bizActivityMapper = mock(BizActivityMapper.class);
        bizActivityScopeMapper = mock(BizActivityScopeMapper.class);
        bizActivitySignupMapper = mock(BizActivitySignupMapper.class);
        dataScopeService = mock(DataScopeService.class);
        sysOrgMapper = mock(SysOrgMapper.class);
        activitySignupLimiter = mock(ActivitySignupLimiter.class);
        fileBindingService = mock(FileBindingService.class);
        fileAttachmentService = mock(FileAttachmentService.class);
        markdownRenderService = mock(MarkdownRenderService.class);
        activityService = new ActivityServiceImpl(
            bizActivityMapper,
            bizActivityScopeMapper,
            bizActivitySignupMapper,
            dataScopeService,
            sysOrgMapper,
            activitySignupLimiter,
            fileBindingService,
            fileAttachmentService,
            markdownRenderService
        );
        mockPrincipal();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRenderMarkdownAndAttachmentsOnDetail() {
        BizActivity activity = buildActivity(200L, 1L, 6L, "{\"attachments\":[{\"fileId\":6}]}");
        when(bizActivityMapper.selectById(200L)).thenReturn(activity);
        when(bizActivityScopeMapper.selectByActivityId(200L)).thenReturn(List.of(buildScope("COMPLEX", 3L)));
        when(markdownRenderService.renderToHtml(activity.getContent())).thenReturn("<p>activity-html</p>");
        when(fileAttachmentService.listByAttachmentJson(activity.getAttachmentJson()))
            .thenReturn(List.of(FileAttachmentVO.builder().fileId(6L).originFileName("activity.png").build()));

        ActivityVO result = activityService.detail(200L);

        assertNotNull(result);
        assertEquals("<p>activity-html</p>", result.getContentHtml());
        assertEquals(1, result.getAttachments().size());
        verify(dataScopeService).assertOrgAccessible(1L, 1L);
    }

    @Test
    void shouldBindForUpdateWhenActivityAttachmentsChanged() {
        String oldAttachmentJson = "{\"attachments\":[{\"fileId\":7}]}";
        String newAttachmentJson = "{\"attachments\":[{\"fileId\":8}]}";
        BizActivity activity = buildActivity(201L, 1L, 7L, oldAttachmentJson);
        when(bizActivityMapper.selectById(201L)).thenReturn(activity);
        when(fileBindingService.collectFileIds(8L, newAttachmentJson)).thenReturn(Set.of(8L));

        ActivityUpdateRequest request = new ActivityUpdateRequest();
        request.setId(201L);
        request.setTitle("阶段六活动更新");
        request.setContent("# 新活动内容");
        request.setCoverFileId(8L);
        request.setAttachmentJson(newAttachmentJson);
        request.setActivityStartTime(LocalDateTime.now().plusDays(3));
        request.setActivityEndTime(LocalDateTime.now().plusDays(3).plusHours(2));
        request.setSignupStartTime(LocalDateTime.now().minusHours(1));
        request.setSignupEndTime(LocalDateTime.now().plusDays(2));
        request.setLocation("A馆");
        request.setMaxParticipants(50);
        ActivityScopeItem scopeItem = new ActivityScopeItem();
        scopeItem.setScopeType("COMPLEX");
        scopeItem.setScopeRefId(3L);
        request.setScopeItems(Set.of(scopeItem));

        activityService.update(request);

        verify(fileBindingService).bindForUpdate("ACTIVITY", 201L, 7L, oldAttachmentJson, 8L, newAttachmentJson, 1L);
        verify(dataScopeService).assertRoleScopeGrantable(eq(1L), anySet(), anySet());
        verify(activitySignupLimiter).evict(201L);
    }

    private BizActivity buildActivity(Long id, Long publisherOrgId, Long coverFileId, String attachmentJson) {
        BizActivity activity = new BizActivity();
        activity.setId(id);
        activity.setPublisherOrgId(publisherOrgId);
        activity.setTitle("活动标题");
        activity.setContent("# 活动内容");
        activity.setCoverFileId(coverFileId);
        activity.setAttachmentJson(attachmentJson);
        activity.setStatus("DRAFT");
        activity.setSignupStartTime(LocalDateTime.now().minusDays(1));
        activity.setSignupEndTime(LocalDateTime.now().plusDays(1));
        activity.setActivityStartTime(LocalDateTime.now().plusDays(2));
        activity.setActivityEndTime(LocalDateTime.now().plusDays(2).plusHours(2));
        activity.setMaxParticipants(30);
        activity.setLocation("活动中心");
        return activity;
    }

    private BizActivityScope buildScope(String scopeType, Long scopeRefId) {
        BizActivityScope scope = new BizActivityScope();
        scope.setActivityId(200L);
        scope.setScopeType(scopeType);
        scope.setScopeRefId(scopeRefId);
        return scope;
    }

    private void mockPrincipal() {
        LoginPrincipal principal = LoginPrincipal.builder()
            .userId(1L)
            .username("admin")
            .orgId(1L)
            .superAdmin(true)
            .permissionCodes(Set.of("activity:update", "activity:view"))
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.toAuthorities())
        );
    }
}
