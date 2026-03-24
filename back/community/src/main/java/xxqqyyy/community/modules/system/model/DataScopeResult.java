package xxqqyyy.community.modules.system.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * 数据范围计算结果。
 *
 * @author codex
 * @since 1.0.0
 */
@Data
@Builder
public class DataScopeResult {

    private boolean allAccess;

    @Builder.Default
    private Set<Long> orgIds = new HashSet<>();

    /**
     * 返回安全可读的组织ID集合。
     *
     * @return 组织ID集合
     */
    public Set<Long> getSafeOrgIds() {
        return orgIds == null ? Collections.emptySet() : orgIds;
    }
}

