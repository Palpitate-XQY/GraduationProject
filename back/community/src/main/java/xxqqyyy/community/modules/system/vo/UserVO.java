package xxqqyyy.community.modules.system.vo;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * 用户视图对象。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String phone;

    private String email;

    private Integer status;

    private Long orgId;

    private String orgName;

    private Set<Long> roleIds;

    private Set<String> roleCodes;

    private LocalDateTime createTime;
}

