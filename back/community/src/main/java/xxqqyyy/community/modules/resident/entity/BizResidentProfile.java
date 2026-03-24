package xxqqyyy.community.modules.resident.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 居民档案实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizResidentProfile extends BaseAuditEntity {

    private Long userId;

    private Long communityOrgId;

    private Long complexOrgId;

    private String roomNo;

    private String emergencyContact;

    private String emergencyPhone;
}

