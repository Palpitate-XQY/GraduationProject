package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.infrastructure.storage.FileStorageProperties;
import xxqqyyy.community.modules.system.dto.StorageConfigUpdateRequest;
import xxqqyyy.community.modules.system.entity.SysStorageConfig;
import xxqqyyy.community.modules.system.mapper.SysStorageConfigMapper;
import xxqqyyy.community.modules.system.service.impl.StorageConfigServiceImpl;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 存储配置服务单元测试。
 *
 * @author codex
 * @since 1.0.0
 */
class StorageConfigServiceImplTest {

    private SysStorageConfigMapper sysStorageConfigMapper;
    private FileStorageProperties fileStorageProperties;
    private StorageConfigServiceImpl storageConfigService;

    @BeforeEach
    void setUp() {
        sysStorageConfigMapper = mock(SysStorageConfigMapper.class);
        fileStorageProperties = new FileStorageProperties();
        fileStorageProperties.setDefaultType("LOCAL");
        fileStorageProperties.setLocalBasePath("./storage");
        fileStorageProperties.setLocalAccessBaseUrl("/api/files");
        storageConfigService = new StorageConfigServiceImpl(sysStorageConfigMapper, fileStorageProperties);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldDenyWhenNonSuperAdminQueryConfig() {
        mockPrincipal(false);
        assertThrows(BizException.class, () -> storageConfigService.currentForAdmin());
    }

    @Test
    void shouldUpdateQiniuConfigBySuperAdmin() {
        mockPrincipal(true);
        SysStorageConfig current = new SysStorageConfig();
        current.setId(1L);
        current.setStorageType("LOCAL");
        current.setLocalBasePath("./storage");
        current.setLocalAccessBaseUrl("/api/files");
        current.setQiniuSecretKey("old-secret");
        when(sysStorageConfigMapper.selectCurrent()).thenReturn(current);

        StorageConfigUpdateRequest request = new StorageConfigUpdateRequest();
        request.setStorageType("QINIU");
        request.setQiniuAccessKey("ak-test");
        request.setQiniuSecretKey("sk-test");
        request.setQiniuBucket("bucket-test");
        request.setQiniuDomain("cdn.example.com");
        request.setQiniuRegion("z0");
        request.setRemark("switch to qiniu");

        storageConfigService.update(request);

        ArgumentCaptor<SysStorageConfig> captor = ArgumentCaptor.forClass(SysStorageConfig.class);
        verify(sysStorageConfigMapper).update(captor.capture());
        SysStorageConfig updated = captor.getValue();
        assertEquals("QINIU", updated.getStorageType());
        assertEquals("ak-test", updated.getQiniuAccessKey());
        assertEquals("sk-test", updated.getQiniuSecretKey());
        assertEquals("bucket-test", updated.getQiniuBucket());
        assertEquals("cdn.example.com", updated.getQiniuDomain());
    }

    @Test
    void shouldUseOldSecretWhenNewSecretEmpty() {
        mockPrincipal(true);
        SysStorageConfig current = new SysStorageConfig();
        current.setId(1L);
        current.setStorageType("QINIU");
        current.setQiniuSecretKey("old-secret");
        when(sysStorageConfigMapper.selectCurrent()).thenReturn(current);

        StorageConfigUpdateRequest request = new StorageConfigUpdateRequest();
        request.setStorageType("QINIU");
        request.setQiniuAccessKey("ak-test");
        request.setQiniuSecretKey(" ");
        request.setQiniuBucket("bucket-test");
        request.setQiniuDomain("cdn.example.com");

        storageConfigService.update(request);

        ArgumentCaptor<SysStorageConfig> captor = ArgumentCaptor.forClass(SysStorageConfig.class);
        verify(sysStorageConfigMapper).update(captor.capture());
        assertEquals("old-secret", captor.getValue().getQiniuSecretKey());
    }

    private void mockPrincipal(boolean superAdmin) {
        LoginPrincipal principal = LoginPrincipal.builder()
            .userId(1L)
            .username("admin")
            .superAdmin(superAdmin)
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.toAuthorities())
        );
    }
}

