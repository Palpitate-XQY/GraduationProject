package xxqqyyy.community.common.model;

import java.io.Serializable;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * 当前登录用户上下文对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class CurrentLoginUser implements Serializable {

    private Long userId;

    private String username;

    private String nickname;

    private Long orgId;

    private Set<String> roleCodes;

    private Set<String> permissionCodes;
}

