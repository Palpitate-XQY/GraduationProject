package xxqqyyy.community.modules.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import xxqqyyy.community.common.model.BaseAuditEntity;

/**
 * 系统用户实体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseAuditEntity {

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String email;

    private Integer status;

    private Long orgId;

    private Integer mustChangePassword;
}

