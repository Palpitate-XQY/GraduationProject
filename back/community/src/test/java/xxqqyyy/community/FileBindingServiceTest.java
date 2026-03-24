package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.file.entity.BizFileInfo;
import xxqqyyy.community.modules.file.mapper.BizFileInfoMapper;
import xxqqyyy.community.modules.file.service.FileBindingService;

/**
 * 文件业务绑定服务单元测试。
 *
 * @author codex
 * @since 1.0.0
 */
class FileBindingServiceTest {

    private BizFileInfoMapper bizFileInfoMapper;
    private FileBindingService fileBindingService;

    @BeforeEach
    void setUp() {
        bizFileInfoMapper = mock(BizFileInfoMapper.class);
        fileBindingService = new FileBindingService(bizFileInfoMapper, new ObjectMapper());
    }

    @Test
    void shouldCollectFileIdsFromCoverAndAttachmentJson() {
        String attachmentJson = """
            {
              "attachments": [
                {"fileId": 11},
                {"id": "12", "fileName": "demo.png"}
              ]
            }
            """;
        Set<Long> ids = fileBindingService.collectFileIds(10L, attachmentJson);
        assertEquals(Set.of(10L, 11L, 12L), ids);
    }

    @Test
    void shouldThrowWhenFileAlreadyBoundOtherBiz() {
        BizFileInfo fileInfo = new BizFileInfo();
        fileInfo.setId(100L);
        fileInfo.setBizType("NOTICE");
        fileInfo.setBizId(9L);
        when(bizFileInfoMapper.selectByIds(Set.of(100L))).thenReturn(List.of(fileInfo));

        assertThrows(
            BizException.class,
            () -> fileBindingService.bindForCreate("ACTIVITY", 1L, 100L, null, 1L)
        );
    }

    @Test
    void shouldBindWhenFilesValidAndUnbound() {
        BizFileInfo fileInfo = new BizFileInfo();
        fileInfo.setId(200L);
        when(bizFileInfoMapper.selectByIds(Set.of(200L))).thenReturn(List.of(fileInfo));

        fileBindingService.bindForCreate("NOTICE", 88L, 200L, null, 1L);

        verify(bizFileInfoMapper).updateBizBind(200L, "NOTICE", 88L, 1L);
    }
}

