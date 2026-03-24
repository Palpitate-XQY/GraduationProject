package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.infrastructure.storage.FileStorageProperties;
import xxqqyyy.community.infrastructure.storage.FileStorageProvider;
import xxqqyyy.community.infrastructure.storage.FileStorageRouter;
import xxqqyyy.community.infrastructure.storage.model.StorageConfigSnapshot;
import xxqqyyy.community.infrastructure.storage.model.StoredFileResult;
import xxqqyyy.community.modules.file.entity.BizFileInfo;
import xxqqyyy.community.modules.file.enums.FileStorageTypeEnum;
import xxqqyyy.community.modules.file.mapper.BizFileInfoMapper;
import xxqqyyy.community.modules.file.service.impl.FileServiceImpl;
import xxqqyyy.community.modules.file.vo.FileInfoVO;
import xxqqyyy.community.modules.system.service.StorageConfigService;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 文件服务单元测试。
 *
 * @author codex
 * @since 1.0.0
 */
class FileServiceImplTest {

    private BizFileInfoMapper bizFileInfoMapper;
    private StorageConfigService storageConfigService;
    private FileStorageRouter fileStorageRouter;
    private FileStorageProvider fileStorageProvider;
    private FileStorageProperties fileStorageProperties;
    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        bizFileInfoMapper = mock(BizFileInfoMapper.class);
        storageConfigService = mock(StorageConfigService.class);
        fileStorageRouter = mock(FileStorageRouter.class);
        fileStorageProvider = mock(FileStorageProvider.class);
        fileStorageProperties = new FileStorageProperties();
        fileStorageProperties.setDefaultType("LOCAL");
        fileStorageProperties.setMaxFileSizeBytes(20 * 1024 * 1024);
        fileService = new FileServiceImpl(
            bizFileInfoMapper,
            storageConfigService,
            fileStorageRouter,
            fileStorageProperties
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUploadFileSuccess() throws Exception {
        mockPrincipal();
        StorageConfigSnapshot snapshot = StorageConfigSnapshot.builder()
            .storageType("LOCAL")
            .localAccessBaseUrl("/api/files")
            .localBasePath("./storage")
            .build();
        when(storageConfigService.currentSnapshot()).thenReturn(snapshot);
        when(fileStorageRouter.route(FileStorageTypeEnum.LOCAL)).thenReturn(fileStorageProvider);
        when(fileStorageProvider.store(any(), any(), any())).thenReturn(StoredFileResult.builder().filePath("2026/03/24/test.png").build());
        when(fileStorageProvider.buildAccessUrl(any(), any(), any())).thenReturn("/api/files/100/download");
        doAnswer(invocation -> {
            BizFileInfo fileInfo = invocation.getArgument(0);
            fileInfo.setId(100L);
            return 1;
        }).when(bizFileInfoMapper).insert(any(BizFileInfo.class));

        MockMultipartFile file = new MockMultipartFile("file", "notice.png", "image/png", "test-data".getBytes());
        FileInfoVO result = fileService.upload(file, "NOTICE", 1L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("LOCAL", result.getStorageType());
        verify(fileStorageProvider).store(eq(file), any(), eq(snapshot));
    }

    @Test
    void shouldThrowWhenUploadFileEmpty() {
        mockPrincipal();
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        assertThrows(BizException.class, () -> fileService.upload(file, null, null));
    }

    private void mockPrincipal() {
        LoginPrincipal principal = LoginPrincipal.builder()
            .userId(1L)
            .username("admin")
            .superAdmin(true)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.toAuthorities())
        );
    }
}

