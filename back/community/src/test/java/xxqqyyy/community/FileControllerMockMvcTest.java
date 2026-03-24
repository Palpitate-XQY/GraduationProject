package xxqqyyy.community;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.file.service.FileService;
import xxqqyyy.community.modules.file.vo.FileInfoVO;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 文件控制器 MockMvc 测试。
 *
 * @author codex
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
class FileControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Test
    void shouldUploadFileSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "notice.md", "text/markdown", "# stage6".getBytes());
        FileInfoVO fileInfoVO = FileInfoVO.builder()
            .id(100L)
            .bizType("NOTICE")
            .bizId(1L)
            .storageType("LOCAL")
            .fileName("notice.md")
            .originFileName("notice.md")
            .fileSize(8L)
            .contentType("text/markdown")
            .accessUrl("/api/files/100/download")
            .uploaderId(1L)
            .createTime(LocalDateTime.now())
            .build();
        when(fileService.upload(any(), eq("NOTICE"), eq(1L))).thenReturn(fileInfoVO);

        mockMvc.perform(
                multipart("/api/files/upload")
                    .file(file)
                    .param("bizType", "NOTICE")
                    .param("bizId", "1")
                    .with(authentication(buildAuthentication(Set.of("file:upload"))))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.id").value(100))
            .andExpect(jsonPath("$.data.storageType").value("LOCAL"));
    }

    @Test
    void shouldRejectWhenNoPermission() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "notice.md", "text/markdown", "# stage6".getBytes());
        mockMvc.perform(
                multipart("/api/files/upload")
                    .file(file)
                    .param("bizType", "NOTICE")
                    .with(authentication(buildAuthentication(Set.of("notice:list"))))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(403));
        verifyNoInteractions(fileService);
    }

    @Test
    void shouldValidateEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        when(fileService.upload(any(), any(), any())).thenThrow(new BizException(ErrorCode.BAD_REQUEST, "上传文件不能为空"));

        mockMvc.perform(
                multipart("/api/files/upload")
                    .file(emptyFile)
                    .param("bizType", "NOTICE")
                    .with(authentication(buildAuthentication(Set.of("file:upload"))))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }

    private Authentication buildAuthentication(Set<String> permissions) {
        LoginPrincipal principal = LoginPrincipal.builder()
            .userId(1L)
            .username("admin")
            .orgId(1L)
            .superAdmin(true)
            .permissionCodes(permissions)
            .build();
        return new UsernamePasswordAuthenticationToken(principal, null, principal.toAuthorities());
    }
}
