package xxqqyyy.community.modules.resident.service;

import xxqqyyy.community.modules.resident.dto.ResidentProfileAdminUpdateRequest;
import xxqqyyy.community.modules.resident.dto.ResidentProfileMyUpdateRequest;
import xxqqyyy.community.modules.resident.vo.ResidentProfileVO;

/**
 * 居民档案服务。
 *
 * @author codex
 * @since 1.0.0
 */
public interface ResidentProfileService {

    ResidentProfileVO getMyProfile();

    void updateMyProfile(ResidentProfileMyUpdateRequest request);

    ResidentProfileVO getByUserId(Long userId);

    void upsertByAdmin(ResidentProfileAdminUpdateRequest request);
}
