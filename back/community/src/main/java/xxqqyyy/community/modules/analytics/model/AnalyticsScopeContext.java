package xxqqyyy.community.modules.analytics.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * Runtime scope context for analytics query and cache.
 */
@Data
@Builder
public class AnalyticsScopeContext {

    private String scopeKey;

    private String scopeKind;

    private boolean allAccess;

    private Long userId;

    @Builder.Default
    private Set<Long> orgIds = new HashSet<>();

    public Set<Long> getSafeOrgIds() {
        return orgIds == null ? Collections.emptySet() : orgIds;
    }
}

