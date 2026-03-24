package xxqqyyy.community.modules.file.service;

import java.util.List;
import xxqqyyy.community.modules.file.vo.FileAttachmentVO;

/**
 * 业务附件查询服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface FileAttachmentService {

    /**
     * 按附件JSON解析并返回结构化附件信息。
     *
     * @param attachmentJson 附件JSON
     * @return 附件列表
     */
    List<FileAttachmentVO> listByAttachmentJson(String attachmentJson);
}

