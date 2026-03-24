package xxqqyyy.community.modules.system.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xxqqyyy.community.modules.activity.mapper.BizActivityMapper;
import xxqqyyy.community.modules.notice.mapper.BizNoticeMapper;
import xxqqyyy.community.modules.repair.mapper.BizRepairOrderMapper;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
import xxqqyyy.community.modules.system.model.DataScopeResult;
import xxqqyyy.community.modules.system.service.DashboardService;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.modules.system.vo.DashboardCardVO;
import xxqqyyy.community.modules.system.vo.DashboardOverviewVO;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 首页看板服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final List<String> REPAIR_OPEN_STATUS_LIST =
        List.of("PENDING_ACCEPT", "ACCEPTED", "ASSIGNED", "PROCESSING", "WAIT_CONFIRM", "REOPENED");

    private static final List<String> MAINTAINER_WAIT_TAKE = List.of("ASSIGNED");
    private static final List<String> MAINTAINER_PROCESSING = List.of("PROCESSING");
    private static final List<String> MAINTAINER_WAIT_CONFIRM = List.of("WAIT_CONFIRM");
    private static final List<String> MAINTAINER_REOPENED = List.of("REOPENED");

    private static final List<String> RESIDENT_PENDING = List.of("PENDING_ACCEPT", "ACCEPTED", "ASSIGNED", "PROCESSING", "WAIT_CONFIRM", "REOPENED");
    private static final List<String> RESIDENT_FINISHED = List.of("DONE", "CLOSED");

    private final SysUserMapper sysUserMapper;
    private final BizNoticeMapper bizNoticeMapper;
    private final BizActivityMapper bizActivityMapper;
    private final BizRepairOrderMapper bizRepairOrderMapper;
    private final DataScopeService dataScopeService;

    @Override
    public DashboardOverviewVO overview() {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        DashboardPolicy policy = resolvePolicy(principal);

        List<DashboardCardVO> cards;
        if ("maintainer_dashboard".equals(policy.dashboardCode())) {
            cards = buildMaintainerCards(principal.getUserId());
        } else if ("resident_dashboard".equals(policy.dashboardCode())) {
            cards = buildResidentCards(principal.getUserId());
        } else {
            DataScopeResult scopeResult = dataScopeService.resolveByUserId(principal.getUserId());
            cards = buildScopedCards(scopeResult.isAllAccess(), scopeResult.getSafeOrgIds());
        }

        return DashboardOverviewVO.builder()
            .dashboardCode(policy.dashboardCode())
            .pagePackCode(policy.pagePackCode())
            .defaultHomeRoute(policy.defaultHomeRoute())
            .roleCodes(principal.getRoleCodes())
            .permissionCodes(principal.getPermissionCodes())
            .cards(cards)
            .build();
    }

    private List<DashboardCardVO> buildScopedCards(boolean allAccess, Collection<Long> orgIds) {
        long userCount = sysUserMapper.countByScope(allAccess, orgIds);
        long noticeCount = bizNoticeMapper.countPublished(allAccess, orgIds);
        long activityCount = bizActivityMapper.countPublished(allAccess, orgIds);
        long openRepairCount = bizRepairOrderMapper.countByStatusInScope(REPAIR_OPEN_STATUS_LIST, allAccess, orgIds);

        List<DashboardCardVO> cards = new ArrayList<>(4);
        cards.add(buildCard("users_total", "辖区用户数", userCount, "当前数据范围内未删除用户数量"));
        cards.add(buildCard("notice_published", "已发布公告", noticeCount, "当前数据范围内已发布公告数量"));
        cards.add(buildCard("activity_published", "已发布活动", activityCount, "当前数据范围内已发布活动数量"));
        cards.add(buildCard("repair_open", "待处理工单", openRepairCount, "待受理到待确认阶段的工单数量"));
        return cards;
    }

    private List<DashboardCardVO> buildMaintainerCards(Long userId) {
        long waitTakeCount = bizRepairOrderMapper.countByMaintainerAndStatus(userId, MAINTAINER_WAIT_TAKE);
        long processingCount = bizRepairOrderMapper.countByMaintainerAndStatus(userId, MAINTAINER_PROCESSING);
        long waitConfirmCount = bizRepairOrderMapper.countByMaintainerAndStatus(userId, MAINTAINER_WAIT_CONFIRM);
        long reopenedCount = bizRepairOrderMapper.countByMaintainerAndStatus(userId, MAINTAINER_REOPENED);
        List<DashboardCardVO> cards = new ArrayList<>(4);
        cards.add(buildCard("repair_wait_take", "待接单", waitTakeCount, "已分派给我但尚未接单"));
        cards.add(buildCard("repair_processing", "处理中", processingCount, "我当前处理中工单"));
        cards.add(buildCard("repair_wait_confirm", "待居民确认", waitConfirmCount, "已提交结果等待居民确认"));
        cards.add(buildCard("repair_reopened", "重新处理", reopenedCount, "居民反馈未解决重新打开工单"));
        return cards;
    }

    private List<DashboardCardVO> buildResidentCards(Long userId) {
        long pendingCount = bizRepairOrderMapper.countByResidentAndStatus(userId, RESIDENT_PENDING);
        long finishedCount = bizRepairOrderMapper.countByResidentAndStatus(userId, RESIDENT_FINISHED);
        List<DashboardCardVO> cards = new ArrayList<>(2);
        cards.add(buildCard("my_repair_pending", "我的处理中报修", pendingCount, "包含待受理、处理中、待确认及重新处理"));
        cards.add(buildCard("my_repair_finished", "我的已完成报修", finishedCount, "包含已完成与已关闭工单"));
        return cards;
    }

    private DashboardCardVO buildCard(String code, String title, long value, String description) {
        return DashboardCardVO.builder()
            .code(code)
            .title(title)
            .value(value)
            .description(description)
            .build();
    }

    private DashboardPolicy resolvePolicy(LoginPrincipal principal) {
        DashboardPolicy explicitPolicy = resolveByRolePriority(principal.getRoleCodes());
        if (explicitPolicy != null) {
            return explicitPolicy;
        }
        Set<String> permissions = principal.getPermissionCodes();
        if (permissions.contains("sys:user:list") || permissions.contains("sys:role:list")) {
            return new DashboardPolicy("system_admin_dashboard", "pack_system_admin", "/dashboard/system-admin");
        }
        if (permissions.contains("repair:manage:list") && permissions.contains("repair:assign")) {
            return new DashboardPolicy("property_service_dashboard", "pack_property_service", "/dashboard/property-service");
        }
        if (permissions.contains("repair:take") || permissions.contains("repair:process") || permissions.contains("repair:submit")) {
            return new DashboardPolicy("maintainer_dashboard", "pack_maintainer", "/dashboard/maintainer");
        }
        if (permissions.contains("notice:resident:list") || permissions.contains("activity:resident:list")) {
            return new DashboardPolicy("resident_dashboard", "pack_resident", "/dashboard/resident");
        }
        return new DashboardPolicy("general_dashboard", "pack_general", "/dashboard/general");
    }

    private DashboardPolicy resolveByRolePriority(Set<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return null;
        }
        Map<String, Integer> priorityMap = new HashMap<>();
        priorityMap.put("SUPER_ADMIN", 100);
        priorityMap.put("STREET_ADMIN", 90);
        priorityMap.put("COMMUNITY_ADMIN", 80);
        priorityMap.put("PROPERTY_ADMIN", 70);
        priorityMap.put("MAINTAINER", 60);
        priorityMap.put("RESIDENT", 10);

        return roleCodes.stream()
            .sorted(Comparator.comparingInt(code -> -priorityMap.getOrDefault(code, 0)))
            .map(this::mapRoleToPolicy)
            .filter(policy -> policy != null)
            .findFirst()
            .orElse(null);
    }

    private DashboardPolicy mapRoleToPolicy(String roleCode) {
        return switch (roleCode) {
            case "SUPER_ADMIN" -> new DashboardPolicy("super_admin_dashboard", "pack_super_admin", "/dashboard/super-admin");
            case "STREET_ADMIN" -> new DashboardPolicy("street_admin_dashboard", "pack_street_admin", "/dashboard/street-admin");
            case "COMMUNITY_ADMIN" -> new DashboardPolicy("community_admin_dashboard", "pack_community_admin", "/dashboard/community-admin");
            case "PROPERTY_ADMIN" -> new DashboardPolicy("property_admin_dashboard", "pack_property_admin", "/dashboard/property-admin");
            case "MAINTAINER" -> new DashboardPolicy("maintainer_dashboard", "pack_maintainer", "/dashboard/maintainer");
            case "RESIDENT" -> new DashboardPolicy("resident_dashboard", "pack_resident", "/dashboard/resident");
            default -> null;
        };
    }

    private record DashboardPolicy(
        String dashboardCode,
        String pagePackCode,
        String defaultHomeRoute
    ) {
    }
}
