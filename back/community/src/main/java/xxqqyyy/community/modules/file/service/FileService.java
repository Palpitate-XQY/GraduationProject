package xxqqyyy.community.modules.file.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import xxqqyyy.community.modules.file.entity.BizFileInfo;
import xxqqyyy.community.modules.file.vo.FileInfoVO;

/**
 * 文件服务接口。
 *
 * @author codex
 * @since 1.0.0
 */
public interface FileService {

    /**
     * 上传文件。
     *
     * @param file 文件
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @return 文件信息
     */
    FileInfoVO upload(MultipartFile file, String bizType, Long bizId);

    /**
     * 查询文件详情。
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    FileInfoVO getById(Long fileId);

    /**
     * 下载文件，按存储方式自动处理（本地直出/七牛跳转）。
     *
     * @param fileId 文件ID
     * @param response 响应对象
     */
    void download(Long fileId, HttpServletResponse response);

    /**
     * 查询文件实体。
     *
     * @param fileId 文件ID
     * @return 文件实体
     */
    BizFileInfo requireById(Long fileId);
}

