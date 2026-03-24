package xxqqyyy.community.modules.repair.service;

import java.util.Set;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.repair.enums.RepairStatusEnum;

/**
 * 报修状态机工具类，集中维护合法流转规则。
 *
 * @author codex
 * @since 1.0.0
 */
public final class RepairStateMachine {

    private static final Set<RepairStatusEnum> ACCEPT_FROM = Set.of(RepairStatusEnum.PENDING_ACCEPT, RepairStatusEnum.REOPENED);
    private static final Set<RepairStatusEnum> REJECT_FROM = Set.of(RepairStatusEnum.PENDING_ACCEPT, RepairStatusEnum.ACCEPTED);
    private static final Set<RepairStatusEnum> ASSIGN_FROM = Set.of(RepairStatusEnum.ACCEPTED, RepairStatusEnum.REOPENED);
    private static final Set<RepairStatusEnum> TAKE_FROM = Set.of(RepairStatusEnum.ASSIGNED);
    private static final Set<RepairStatusEnum> PROCESS_FROM = Set.of(RepairStatusEnum.PROCESSING);
    private static final Set<RepairStatusEnum> SUBMIT_FROM = Set.of(RepairStatusEnum.PROCESSING);
    private static final Set<RepairStatusEnum> CONFIRM_FROM = Set.of(RepairStatusEnum.WAIT_CONFIRM);
    private static final Set<RepairStatusEnum> CLOSE_FROM = Set.of(RepairStatusEnum.DONE);
    private static final Set<RepairStatusEnum> REOPEN_FROM = Set.of(RepairStatusEnum.WAIT_CONFIRM, RepairStatusEnum.DONE);
    private static final Set<RepairStatusEnum> EVALUATE_FROM = Set.of(RepairStatusEnum.DONE, RepairStatusEnum.CLOSED);
    private static final Set<RepairStatusEnum> URGE_FROM = Set.of(
        RepairStatusEnum.PENDING_ACCEPT,
        RepairStatusEnum.ACCEPTED,
        RepairStatusEnum.ASSIGNED,
        RepairStatusEnum.PROCESSING,
        RepairStatusEnum.WAIT_CONFIRM,
        RepairStatusEnum.REOPENED
    );

    private RepairStateMachine() {
    }

    public static void assertCanAccept(RepairStatusEnum current) {
        assertInSet(current, ACCEPT_FROM, "当前状态不可受理");
    }

    public static void assertCanReject(RepairStatusEnum current) {
        assertInSet(current, REJECT_FROM, "当前状态不可驳回");
    }

    public static void assertCanAssign(RepairStatusEnum current) {
        assertInSet(current, ASSIGN_FROM, "当前状态不可分派");
    }

    public static void assertCanTake(RepairStatusEnum current) {
        assertInSet(current, TAKE_FROM, "当前状态不可接单");
    }

    public static void assertCanProcess(RepairStatusEnum current) {
        assertInSet(current, PROCESS_FROM, "当前状态不可处理");
    }

    public static void assertCanSubmit(RepairStatusEnum current) {
        assertInSet(current, SUBMIT_FROM, "当前状态不可提交处理结果");
    }

    public static void assertCanConfirm(RepairStatusEnum current) {
        assertInSet(current, CONFIRM_FROM, "当前状态不可确认");
    }

    public static void assertCanClose(RepairStatusEnum current) {
        assertInSet(current, CLOSE_FROM, "当前状态不可关闭");
    }

    public static void assertCanReopen(RepairStatusEnum current) {
        assertInSet(current, REOPEN_FROM, "当前状态不可重新处理");
    }

    public static void assertCanEvaluate(RepairStatusEnum current) {
        assertInSet(current, EVALUATE_FROM, "当前状态不可评价");
    }

    public static void assertCanUrge(RepairStatusEnum current) {
        assertInSet(current, URGE_FROM, "当前状态不可催单");
    }

    private static void assertInSet(RepairStatusEnum current, Set<RepairStatusEnum> validSet, String message) {
        if (current == null || !validSet.contains(current)) {
            throw new BizException(ErrorCode.ILLEGAL_STATE_FLOW, message);
        }
    }
}
