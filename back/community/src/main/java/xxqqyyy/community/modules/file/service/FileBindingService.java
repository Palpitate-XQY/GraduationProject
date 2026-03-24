package xxqqyyy.community.modules.file.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.file.entity.BizFileInfo;
import xxqqyyy.community.modules.file.mapper.BizFileInfoMapper;

/**
 * 文件业务绑定服务，负责附件文件的校验与绑定回写。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class FileBindingService {

    private static final String BIZ_TYPE_UNBOUND = "UNBOUND";

    private final BizFileInfoMapper bizFileInfoMapper;
    private final ObjectMapper objectMapper;

    /**
     * 新建业务时绑定文件。
     *
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param coverFileId 封面文件ID
     * @param attachmentJson 附件JSON
     * @param operatorUserId 操作人
     */
    public void bindForCreate(String bizType, Long bizId, Long coverFileId, String attachmentJson, Long operatorUserId) {
        Set<Long> targetIds = collectFileIds(coverFileId, attachmentJson);
        bindTargetFileIds(bizType, bizId, targetIds, operatorUserId);
    }

    /**
     * 更新业务时重绑定文件（会解绑本次更新移除的文件）。
     *
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param oldCoverFileId 旧封面文件ID
     * @param oldAttachmentJson 旧附件JSON
     * @param newCoverFileId 新封面文件ID
     * @param newAttachmentJson 新附件JSON
     * @param operatorUserId 操作人
     */
    public void bindForUpdate(
        String bizType,
        Long bizId,
        Long oldCoverFileId,
        String oldAttachmentJson,
        Long newCoverFileId,
        String newAttachmentJson,
        Long operatorUserId
    ) {
        Set<Long> oldIds = collectFileIds(oldCoverFileId, oldAttachmentJson);
        Set<Long> newIds = collectFileIds(newCoverFileId, newAttachmentJson);
        bindTargetFileIds(bizType, bizId, newIds, operatorUserId);
        unbindRemovedFileIds(bizType, bizId, oldIds, newIds, operatorUserId);
    }

    /**
     * 提取附件文件ID集合，供业务层做快速校验。
     *
     * @param coverFileId 封面文件ID
     * @param attachmentJson 附件JSON
     * @return 文件ID集合
     */
    public Set<Long> collectFileIds(Long coverFileId, String attachmentJson) {
        Set<Long> fileIds = new LinkedHashSet<>();
        if (coverFileId != null) {
            if (coverFileId <= 0) {
                throw new BizException(ErrorCode.BAD_REQUEST, "封面文件ID非法");
            }
            fileIds.add(coverFileId);
        }
        fileIds.addAll(parseAttachmentFileIds(attachmentJson));
        return fileIds;
    }

    /**
     * 校验文件ID集合是否全部存在。
     *
     * @param fileIds 文件ID集合
     */
    public void assertFileIdsValid(Set<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        int validCount = bizFileInfoMapper.selectByIds(fileIds).size();
        if (validCount != fileIds.size()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "附件中存在无效文件ID");
        }
    }

    private void bindTargetFileIds(String bizType, Long bizId, Set<Long> targetIds, Long operatorUserId) {
        if (targetIds.isEmpty()) {
            return;
        }
        List<BizFileInfo> files = bizFileInfoMapper.selectByIds(targetIds);
        if (files.size() != targetIds.size()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "附件中存在无效文件ID");
        }
        Map<Long, BizFileInfo> fileMap = files.stream().collect(Collectors.toMap(BizFileInfo::getId, Function.identity()));
        for (Long fileId : targetIds) {
            BizFileInfo fileInfo = fileMap.get(fileId);
            if (fileInfo == null) {
                throw new BizException(ErrorCode.BAD_REQUEST, "附件中存在无效文件ID");
            }
            if (fileInfo.getBizId() != null && !isSameBiz(fileInfo, bizType, bizId)) {
                throw new BizException(ErrorCode.CONFLICT, "文件已绑定其他业务，fileId=" + fileId);
            }
        }
        for (Long fileId : targetIds) {
            bizFileInfoMapper.updateBizBind(fileId, bizType, bizId, operatorUserId);
        }
    }

    private void unbindRemovedFileIds(String bizType, Long bizId, Set<Long> oldIds, Set<Long> newIds, Long operatorUserId) {
        if (oldIds.isEmpty()) {
            return;
        }
        Set<Long> removedIds = new LinkedHashSet<>(oldIds);
        removedIds.removeAll(newIds);
        if (removedIds.isEmpty()) {
            return;
        }
        List<BizFileInfo> removedFiles = bizFileInfoMapper.selectByIds(removedIds);
        for (BizFileInfo fileInfo : removedFiles) {
            if (isSameBiz(fileInfo, bizType, bizId)) {
                bizFileInfoMapper.updateBizBind(fileInfo.getId(), BIZ_TYPE_UNBOUND, null, operatorUserId);
            }
        }
    }

    private boolean isSameBiz(BizFileInfo fileInfo, String bizType, Long bizId) {
        if (fileInfo == null || fileInfo.getBizId() == null) {
            return false;
        }
        return StringUtils.equalsIgnoreCase(fileInfo.getBizType(), bizType) && fileInfo.getBizId().equals(bizId);
    }

    private Set<Long> parseAttachmentFileIds(String attachmentJson) {
        if (StringUtils.isBlank(attachmentJson)) {
            return Set.of();
        }
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(attachmentJson);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.BAD_REQUEST, "附件JSON格式非法");
        }
        Set<Long> fileIds = new LinkedHashSet<>();
        collectFromNode(rootNode, fileIds);
        return fileIds;
    }

    private void collectFromNode(JsonNode node, Set<Long> fileIds) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                collectFromNode(item, fileIds);
            }
            return;
        }
        if (node.isNumber() || node.isTextual()) {
            Long value = parseFileIdValue(node);
            if (value != null) {
                fileIds.add(value);
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }

        if (node.has("fileId")) {
            Long value = parseFileIdValue(node.get("fileId"));
            if (value != null) {
                fileIds.add(value);
            }
        }
        if (node.has("id") && likelyFileObject(node)) {
            Long value = parseFileIdValue(node.get("id"));
            if (value != null) {
                fileIds.add(value);
            }
        }
        if (node.has("fileIds")) {
            collectFromNode(node.get("fileIds"), fileIds);
        }
        if (node.has("attachments")) {
            collectFromNode(node.get("attachments"), fileIds);
        }
        List<String> fieldNames = new ArrayList<>();
        node.fieldNames().forEachRemaining(fieldNames::add);
        for (String fieldName : fieldNames) {
            String normalized = fieldName.toLowerCase(Locale.ROOT);
            if ("fileid".equals(normalized) || "id".equals(normalized) || "fileids".equals(normalized) || "attachments".equals(normalized)) {
                continue;
            }
            JsonNode child = node.get(fieldName);
            if (child != null && (child.isArray() || child.isObject())) {
                collectFromNode(child, fileIds);
            }
        }
    }

    private boolean likelyFileObject(JsonNode node) {
        return node.has("fileName")
            || node.has("originFileName")
            || node.has("filePath")
            || node.has("url")
            || node.has("accessUrl");
    }

    private Long parseFileIdValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        long value;
        if (node.isNumber()) {
            value = node.asLong();
        } else if (node.isTextual()) {
            String text = StringUtils.trim(node.asText());
            if (StringUtils.isBlank(text)) {
                return null;
            }
            try {
                value = Long.parseLong(text);
            } catch (NumberFormatException ex) {
                throw new BizException(ErrorCode.BAD_REQUEST, "附件JSON中存在非法文件ID");
            }
        } else {
            return null;
        }
        if (value <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "附件JSON中存在非法文件ID");
        }
        return value;
    }
}
