package xxqqyyy.community.modules.file.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import xxqqyyy.community.modules.file.entity.BizFileInfo;
import xxqqyyy.community.modules.file.vo.FileInfoVO;

/**
 * File service interface.
 */
public interface FileService {

    /**
     * Upload file.
     */
    FileInfoVO upload(MultipartFile file, String bizType, Long bizId);

    /**
     * Query file detail.
     */
    FileInfoVO getById(Long fileId);

    /**
     * Download file as attachment.
     */
    void download(Long fileId, HttpServletResponse response);

    /**
     * Preview file for inline usage.
     */
    void preview(Long fileId, HttpServletResponse response);

    /**
     * Query file entity.
     */
    BizFileInfo requireById(Long fileId);
}
