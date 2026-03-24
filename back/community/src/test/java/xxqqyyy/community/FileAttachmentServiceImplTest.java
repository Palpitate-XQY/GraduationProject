package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xxqqyyy.community.infrastructure.storage.FileStorageProperties;
import xxqqyyy.community.infrastructure.storage.FileStorageProvider;
import xxqqyyy.community.infrastructure.storage.FileStorageRouter;
import xxqqyyy.community.infrastructure.storage.model.StorageConfigSnapshot;
import xxqqyyy.community.modules.file.entity.BizFileInfo;
import xxqqyyy.community.modules.file.enums.FileStorageTypeEnum;
import xxqqyyy.community.modules.file.mapper.BizFileInfoMapper;
import xxqqyyy.community.modules.file.service.FileBindingService;
import xxqqyyy.community.modules.file.service.impl.FileAttachmentServiceImpl;
import xxqqyyy.community.modules.file.vo.FileAttachmentVO;
import xxqqyyy.community.modules.system.service.StorageConfigService;

/**
 * 业务附件查询服务单元测试。
 *
 * @author codex
 * @since 1.0.0
 */
class FileAttachmentServiceImplTest {

    private FileBindingService fileBindingService;
    private BizFileInfoMapper bizFileInfoMapper;
    private StorageConfigService storageConfigService;
    private FileStorageRouter fileStorageRouter;
    private FileStorageProvider localProvider;
    private FileStorageProperties fileStorageProperties;
    private FileAttachmentServiceImpl fileAttachmentService;

    @BeforeEach
    void setUp() {
        fileBindingService = mock(FileBindingService.class);
        bizFileInfoMapper = mock(BizFileInfoMapper.class);
        storageConfigService = mock(StorageConfigService.class);
        fileStorageRouter = mock(FileStorageRouter.class);
        localProvider = mock(FileStorageProvider.class);
        fileStorageProperties = new FileStorageProperties();
        fileStorageProperties.setDefaultType("LOCAL");
        fileAttachmentService = new FileAttachmentServiceImpl(
            fileBindingService,
            bizFileInfoMapper,
            storageConfigService,
            fileStorageRouter,
            fileStorageProperties
        );
    }

    @Test
    void shouldResolveStructuredAttachments() {
        when(fileBindingService.collectFileIds(null, "[{\"fileId\":10}]")).thenReturn(java.util.Set.of(10L));
        BizFileInfo fileInfo = new BizFileInfo();
        fileInfo.setId(10L);
        fileInfo.setFileName("a.png");
        fileInfo.setOriginFileName("origin-a.png");
        fileInfo.setFileSize(123L);
        fileInfo.setContentType("image/png");
        fileInfo.setStorageType("LOCAL");
        fileInfo.setFilePath("2026/03/24/a.png");
        when(bizFileInfoMapper.selectByIds(java.util.Set.of(10L))).thenReturn(List.of(fileInfo));
        when(storageConfigService.currentSnapshot()).thenReturn(StorageConfigSnapshot.builder().storageType("LOCAL").build());
        when(fileStorageRouter.route(FileStorageTypeEnum.LOCAL)).thenReturn(localProvider);
        when(localProvider.buildAccessUrl(any(), any(), any())).thenReturn("/api/files/10/download");

        List<FileAttachmentVO> result = fileAttachmentService.listByAttachmentJson("[{\"fileId\":10}]");
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getFileId());
        assertEquals("/api/files/10/download", result.get(0).getAccessUrl());
    }
}
