package xxqqyyy.community;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.repair.enums.RepairStatusEnum;
import xxqqyyy.community.modules.repair.service.RepairStateMachine;

/**
 * 报修状态机规则测试。
 *
 * @author codex
 * @since 1.0.0
 */
class RepairStateMachineTest {

    @Test
    void shouldAllowHappyPathTransitions() {
        assertDoesNotThrow(() -> RepairStateMachine.assertCanAccept(RepairStatusEnum.PENDING_ACCEPT));
        assertDoesNotThrow(() -> RepairStateMachine.assertCanAssign(RepairStatusEnum.ACCEPTED));
        assertDoesNotThrow(() -> RepairStateMachine.assertCanTake(RepairStatusEnum.ASSIGNED));
        assertDoesNotThrow(() -> RepairStateMachine.assertCanSubmit(RepairStatusEnum.PROCESSING));
        assertDoesNotThrow(() -> RepairStateMachine.assertCanConfirm(RepairStatusEnum.WAIT_CONFIRM));
        assertDoesNotThrow(() -> RepairStateMachine.assertCanClose(RepairStatusEnum.DONE));
    }

    @Test
    void shouldBlockIllegalTransitions() {
        assertThrows(BizException.class, () -> RepairStateMachine.assertCanAccept(RepairStatusEnum.CLOSED));
        assertThrows(BizException.class, () -> RepairStateMachine.assertCanAssign(RepairStatusEnum.PENDING_ACCEPT));
        assertThrows(BizException.class, () -> RepairStateMachine.assertCanTake(RepairStatusEnum.ACCEPTED));
        assertThrows(BizException.class, () -> RepairStateMachine.assertCanSubmit(RepairStatusEnum.ASSIGNED));
        assertThrows(BizException.class, () -> RepairStateMachine.assertCanReopen(RepairStatusEnum.REJECTED));
    }
}
