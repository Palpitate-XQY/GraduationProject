package xxqqyyy.community.modules.repair.service;

import java.util.List;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.repair.dto.RepairAcceptRequest;
import xxqqyyy.community.modules.repair.dto.RepairAssignRequest;
import xxqqyyy.community.modules.repair.dto.RepairCloseRequest;
import xxqqyyy.community.modules.repair.dto.RepairConfirmRequest;
import xxqqyyy.community.modules.repair.dto.RepairCreateRequest;
import xxqqyyy.community.modules.repair.dto.RepairEvaluateRequest;
import xxqqyyy.community.modules.repair.dto.RepairPageQuery;
import xxqqyyy.community.modules.repair.dto.RepairProcessRequest;
import xxqqyyy.community.modules.repair.dto.RepairRejectRequest;
import xxqqyyy.community.modules.repair.dto.RepairReopenRequest;
import xxqqyyy.community.modules.repair.dto.RepairSubmitRequest;
import xxqqyyy.community.modules.repair.dto.RepairTakeRequest;
import xxqqyyy.community.modules.repair.dto.RepairUrgeRequest;
import xxqqyyy.community.modules.repair.vo.RepairOrderLogVO;
import xxqqyyy.community.modules.repair.vo.RepairOrderVO;

/**
 * 报修工单服务接口。
 *
 * @author codex
 * @since 1.0.0
 */
public interface RepairOrderService {

    void create(RepairCreateRequest request);

    PageResult<RepairOrderVO> myPage(RepairPageQuery query);

    RepairOrderVO myDetail(Long orderId);

    PageResult<RepairOrderVO> managePage(RepairPageQuery query);

    RepairOrderVO manageDetail(Long orderId);

    void accept(Long orderId, RepairAcceptRequest request);

    void reject(Long orderId, RepairRejectRequest request);

    void assign(Long orderId, RepairAssignRequest request);

    void take(Long orderId, RepairTakeRequest request);

    void process(Long orderId, RepairProcessRequest request);

    void submit(Long orderId, RepairSubmitRequest request);

    void confirm(Long orderId, RepairConfirmRequest request);

    void reopen(Long orderId, RepairReopenRequest request);

    void evaluate(Long orderId, RepairEvaluateRequest request);

    void urge(Long orderId, RepairUrgeRequest request);

    void close(Long orderId, RepairCloseRequest request);

    List<RepairOrderLogVO> flowLogs(Long orderId);
}
