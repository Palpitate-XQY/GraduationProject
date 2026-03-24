package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import xxqqyyy.community.common.util.MarkdownRenderService;
import xxqqyyy.community.modules.file.service.FileAttachmentService;
import xxqqyyy.community.modules.file.service.FileBindingService;
import xxqqyyy.community.modules.file.vo.FileAttachmentVO;
import xxqqyyy.community.modules.notice.dto.NoticeScopeItem;
import xxqqyyy.community.modules.notice.dto.NoticeUpdateRequest;
import xxqqyyy.community.modules.notice.entity.BizNotice;
import xxqqyyy.community.modules.notice.entity.BizNoticeScope;
import xxqqyyy.community.modules.notice.mapper.BizNoticeMapper;
import xxqqyyy.community.modules.notice.mapper.BizNoticeScopeMapper;
import xxqqyyy.community.modules.notice.service.impl.NoticeServiceImpl;
import xxqqyyy.community.modules.notice.vo.NoticeVO;
import xxqqyyy.community.modules.org.mapper.BizComplexPropertyRelMapper;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 公告服务测试，覆盖 Markdown 渲染与附件绑定行为。
 *
 * @author codex
 * @since 1.0.0
 */
class NoticeServiceImplTest {

    private BizNoticeMapper bizNoticeMapper;
    private BizNoticeScopeMapper bizNoticeScopeMapper;
    private DataScopeService dataScopeService;
    private SysOrgMapper sysOrgMapper;
    private BizComplexPropertyRelMapper bizComplexPropertyRelMapper;
    private FileBindingService fileBindingService;
    private FileAttachmentService fileAttachmentService;
    private MarkdownRenderService markdownRenderService;
    private NoticeServiceImpl noticeService;

    @BeforeEach
    void setUp() {
        bizNoticeMapper = mock(BizNoticeMapper.class);
        bizNoticeScopeMapper = mock(BizNoticeScopeMapper.class);
        dataScopeService = mock(DataScopeService.class);
        sysOrgMapper = mock(SysOrgMapper.class);
        bizComplexPropertyRelMapper = mock(BizComplexPropertyRelMapper.class);
        fileBindingService = mock(FileBindingService.class);
        fileAttachmentService = mock(FileAttachmentService.class);
        markdownRenderService = mock(MarkdownRenderService.class);
        noticeService = new NoticeServiceImpl(
            bizNoticeMapper,
            bizNoticeScopeMapper,
            dataScopeService,
            sysOrgMapper,
            bizComplexPropertyRelMapper,
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
        BizNotice notice = buildNotice(100L, 1L, 9L, "{\"attachments\":[{\"fileId\":9}]}");
        when(bizNoticeMapper.selectById(100L)).thenReturn(notice);
        when(bizNoticeScopeMapper.selectByNoticeId(100L)).thenReturn(List.of(buildScope("COMMUNITY", 2L)));
        when(markdownRenderService.renderToHtml(notice.getContent())).thenReturn("<h1>公告</h1>");
        when(fileAttachmentService.listByAttachmentJson(notice.getAttachmentJson()))
            .thenReturn(List.of(FileAttachmentVO.builder().fileId(9L).originFileName("notice.md").build()));

        NoticeVO result = noticeService.detail(100L);

        assertNotNull(result);
        assertEquals("<h1>公告</h1>", result.getContentHtml());
        assertEquals(1, result.getAttachments().size());
        verify(dataScopeService).assertOrgAccessible(1L, 1L);
    }

    @Test
    void shouldBindForUpdateWhenNoticeAttachmentsChanged() {
        String oldAttachmentJson = "{\"attachments\":[{\"fileId\":1}]}";
        String newAttachmentJson = "{\"attachments\":[{\"fileId\":2},{\"fileId\":3}]}";
        BizNotice notice = buildNotice(101L, 1L, 1L, oldAttachmentJson);
        when(bizNoticeMapper.selectById(101L)).thenReturn(notice);
        when(fileBindingService.collectFileIds(2L, newAttachmentJson)).thenReturn(Set.of(2L, 3L));

        NoticeUpdateRequest request = new NoticeUpdateRequest();
        request.setId(101L);
        request.setNoticeType("STREET_COMMUNITY");
        request.setTitle("更新后的公告");
        request.setContent("# 更新内容");
        request.setCoverFileId(2L);
        request.setAttachmentJson(newAttachmentJson);
        request.setTopFlag(1);
        NoticeScopeItem scopeItem = new NoticeScopeItem();
        scopeItem.setScopeType("COMMUNITY");
        scopeItem.setScopeRefId(2L);
        request.setScopeItems(Set.of(scopeItem));

        noticeService.update(request);

        verify(fileBindingService).bindForUpdate("NOTICE", 101L, 1L, oldAttachmentJson, 2L, newAttachmentJson, 1L);
        verify(dataScopeService).assertRoleScopeGrantable(eq(1L), anySet(), anySet());
        verify(bizNoticeScopeMapper).batchInsert(anyList());
    }

    private BizNotice buildNotice(Long id, Long publisherOrgId, Long coverFileId, String attachmentJson) {
        BizNotice notice = new BizNotice();
        notice.setId(id);
        notice.setPublisherOrgId(publisherOrgId);
        notice.setNoticeType("STREET_COMMUNITY");
        notice.setTitle("公告标题");
        notice.setContent("# 公告内容");
        notice.setCoverFileId(coverFileId);
        notice.setAttachmentJson(attachmentJson);
        notice.setStatus("DRAFT");
        return notice;
    }

    private BizNoticeScope buildScope(String scopeType, Long scopeRefId) {
        BizNoticeScope scope = new BizNoticeScope();
        scope.setNoticeId(100L);
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
            .permissionCodes(Set.of("notice:update", "notice:view"))
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.toAuthorities())
        );
    }
}
