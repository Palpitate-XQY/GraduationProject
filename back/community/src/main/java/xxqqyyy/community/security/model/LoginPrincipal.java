package xxqqyyy.community.security.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import xxqqyyy.community.common.model.CurrentLoginUser;

/**
 * 登录主体对象，作为 SecurityContext 的核心用户信息载体。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class LoginPrincipal implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private String nickname;

    private Long orgId;

    private boolean superAdmin;

    @Builder.Default
    private Set<String> roleCodes = new HashSet<>();

    @Builder.Default
    private Set<String> permissionCodes = new HashSet<>();

    /**
     * 转换为 Spring Security 权限集合。
     *
     * @return GrantedAuthority 集合
     */
    public Set<GrantedAuthority> toAuthorities() {
        Set<String> authorities = new HashSet<>();
        authorities.addAll(permissionCodes);
        authorities.addAll(roleCodes.stream().map(role -> "ROLE_" + role).collect(Collectors.toSet()));
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    /**
     * 转换为公共上下文对象。
     *
     * @return 当前登录用户上下文
     */
    public CurrentLoginUser toCurrentLoginUser() {
        return CurrentLoginUser.builder()
            .userId(userId)
            .username(username)
            .nickname(nickname)
            .orgId(orgId)
            .roleCodes(roleCodes == null ? Collections.emptySet() : roleCodes)
            .permissionCodes(permissionCodes == null ? Collections.emptySet() : permissionCodes)
            .build();
    }
}

