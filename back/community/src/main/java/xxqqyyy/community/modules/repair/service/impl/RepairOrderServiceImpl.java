package xxqqyyy.community.modules.repair.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.org.entity.BizComplexPropertyRel;
import xxqqyyy.community.modules.org.entity.SysOrg;
import xxqqyyy.community.modules.org.enums.OrgTypeEnum;
import xxqqyyy.community.modules.org.mapper.BizComplexPropertyRelMapper;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.file.service.FileBindingService;
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
import xxqqyyy.community.modules.repair.entity.BizRepairAttachment;
import xxqqyyy.community.modules.repair.entity.BizRepairOrder;
import xxqqyyy.community.modules.repair.entity.BizRepairOrderLog;
import xxqqyyy.community.modules.repair.enums.RepairAttachmentTypeEnum;
import xxqqyyy.community.modules.repair.enums.RepairOperationTypeEnum;
import xxqqyyy.community.modules.repair.enums.RepairStatusEnum;
import xxqqyyy.community.modules.repair.mapper.BizRepairAttachmentMapper;
import xxqqyyy.community.modules.repair.mapper.BizRepairOrderLogMapper;
import xxqqyyy.community.modules.repair.mapper.BizRepairOrderMapper;
import xxqqyyy.community.modules.repair.service.RepairOrderService;
import xxqqyyy.community.modules.repair.service.RepairStateMachine;
import xxqqyyy.community.modules.repair.vo.RepairAttachmentVO;
import xxqqyyy.community.modules.repair.vo.RepairOrderLogVO;
import xxqqyyy.community.modules.repair.vo.RepairOrderVO;
import xxqqyyy.community.modules.system.entity.SysUser;
import xxqqyyy.community.modules.system.enums.UserStatusEnum;
import xxqqyyy.community.modules.system.model.DataScopeResult;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 报修工单服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl implements RepairOrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final BizRepairOrderMapper bizRepairOrderMapper;
    private final BizRepairOrderLogMapper bizRepairOrderLogMapper;
    private final BizRepairAttachmentMapper bizRepairAttachmentMapper;
    private final BizComplexPropertyRelMapper bizComplexPropertyRelMapper;
    private final SysOrgMapper sysOrgMapper;
    private final SysUserMapper sysUserMapper;
    private final FileBindingService fileBindingService;
    private final DataScopeService dataScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(RepairCreateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysOrg complexOrg = requireComplexOrg(request.getComplexOrgId());
        assertCreateOrgAccess(principal, complexOrg.getId());
        Long propertyCompanyOrgId = resolvePropertyCompanyOrgId(request.getPropertyCompanyOrgId(), complexOrg.getId());

        BizRepairOrder order = new BizRepairOrder();
        order.setOrderNo(generateOrderNo(principal.getUserId()));
        order.setTitle(request.getTitle());
        order.setDescription(request.getDescription());
        order.setContactPhone(request.getContactPhone());
        order.setRepairAddress(request.getRepairAddress());
        order.setEmergencyLevel(request.getEmergencyLevel());
        order.setExpectHandleTime(request.getExpectHandleTime());
        order.setStatus(RepairStatusEnum.PENDING_ACCEPT.getCode());
        order.setCommunityOrgId(resolveCommunityOrgId(complexOrg));
        order.setComplexOrgId(complexOrg.getId());
        order.setPropertyCompanyOrgId(propertyCompanyOrgId);
        order.setResidentUserId(principal.getUserId());
        order.setUrgeCount(0);
        order.setCreateBy(principal.getUserId());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.insert(order);

        saveAttachments(order.getId(), request.getAttachmentFileIds(), RepairAttachmentTypeEnum.REPORT, principal.getUserId());
        appendLog(
            order.getId(),
            null,
            RepairStatusEnum.PENDING_ACCEPT.getCode(),
            RepairOperationTypeEnum.CREATE,
            "居民发起报修",
            principal.getUserId()
        );
    }

    @Override
    public PageResult<RepairOrderVO> myPage(RepairPageQuery query) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        long total = bizRepairOrderMapper.countMyPage(query, principal.getUserId());
        if (total == 0) {
            return PageResult.empty(query.getCurrent(), query.getSize());
        }
        long offset = (query.getCurrent() - 1) * query.getSize();
        List<RepairOrderVO> records = bizRepairOrderMapper.selectMyPage(query, principal.getUserId(), offset, query.getSize())
            .stream()
            .map(order -> toOrderVO(order, false))
            .toList();
        return PageResult.<RepairOrderVO>builder()
            .records(records)
            .total(total)
            .current(query.getCurrent())
            .size(query.getSize())
            .build();
    }

    @Override
    public RepairOrderVO myDetail(Long orderId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        assertOwner(order, principal.getUserId());
        return toOrderVO(order, true);
    }

    @Override
    public PageResult<RepairOrderVO> managePage(RepairPageQuery query) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        DataScopeResult scope = dataScopeService.resolveByUserId(principal.getUserId());
        long total = bizRepairOrderMapper.countManagePage(query, scope.isAllAccess(), scope.getSafeOrgIds());
        if (total == 0) {
            return PageResult.empty(query.getCurrent(), query.getSize());
        }
        long offset = (query.getCurrent() - 1) * query.getSize();
        List<RepairOrderVO> records = bizRepairOrderMapper.selectManagePage(query, scope.isAllAccess(), scope.getSafeOrgIds(), offset, query.getSize())
            .stream()
            .map(order -> toOrderVO(order, false))
            .toList();
        return PageResult.<RepairOrderVO>builder()
            .records(records)
            .total(total)
            .current(query.getCurrent())
            .size(query.getSize())
            .build();
    }

    @Override
    public RepairOrderVO manageDetail(Long orderId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        assertManageAccess(order, principal.getUserId());
        return toOrderVO(order, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(Long orderId, RepairAcceptRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        assertManageAccess(order, principal.getUserId());
        RepairStatusEnum current = requireStatus(order);
        RepairStateMachine.assertCanAccept(current);

        order.setStatus(RepairStatusEnum.ACCEPTED.getCode());
        order.setAcceptUserId(principal.getUserId());
        order.setAcceptedTime(LocalDateTime.now());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.updateSelective(order);

        appendLog(orderId, current.getCode(), RepairStatusEnum.ACCEPTED.getCode(), RepairOperationTypeEnum.ACCEPT,
            defaultRemark(request == null ? null : request.getRemark(), "受理工单"), principal.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long orderId, RepairRejectRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        assertManageAccess(order, principal.getUserId());
        RepairStatusEnum current = requireStatus(order);
        RepairStateMachine.assertCanReject(current);

        order.setStatus(RepairStatusEnum.REJECTED.getCode());
        order.setRejectReason(request.getReason());
        order.setAcceptUserId(principal.getUserId());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.updateSelective(order);

        appendLog(orderId, current.getCode(), RepairStatusEnum.REJECTED.getCode(), RepairOperationTypeEnum.REJECT,
            request.getReason(), principal.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Long orderId, RepairAssignRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        assertManageAccess(order, principal.getUserId());
        RepairStatusEnum current = requireStatus(order);
        RepairStateMachine.assertCanAssign(current);

        SysUser maintainer = requireEnabledUser(request.getMaintainerUserId());
        dataScopeService.assertOrgAccessible(principal.getUserId(), maintainer.getOrgId());

        order.setStatus(RepairStatusEnum.ASSIGNED.getCode());
        order.setAssignUserId(principal.getUserId());
        order.setMaintainerUserId(request.getMaintainerUserId());
        order.setAssignedTime(LocalDateTime.now());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.updateSelective(order);

        appendLog(orderId, current.getCode(), RepairStatusEnum.ASSIGNED.getCode(), RepairOperationTypeEnum.ASSIGN,
            defaultRemark(request.getRemark(), "分派维修员: " + request.getMaintainerUserId()), principal.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void take(Long orderId, RepairTakeRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        RepairStatusEnum current = requireStatus(order);
        RepairStateMachine.assertCanTake(current);
        assertMaintainer(order, principal.getUserId());

        order.setStatus(RepairStatusEnum.PROCESSING.getCode());
        order.setProcessingTime(LocalDateTime.now());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.updateSelective(order);

        appendLog(orderId, current.getCode(), RepairStatusEnum.PROCESSING.getCode(), RepairOperationTypeEnum.TAKE,
            defaultRemark(request == null ? null : request.getRemark(), "维修员已接单"), principal.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(Long orderId, RepairProcessRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        RepairStatusEnum current = requireStatus(order);
        RepairStateMachine.assertCanProcess(current);
        assertMaintainer(order, principal.getUserId());

        order.setProcessDesc(request.getProcessDesc());
        order.setProcessingTime(LocalDateTime.now());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.updateSelective(order);

        saveAttachments(orderId, request.getAttachmentFileIds(), RepairAttachmentTypeEnum.PROCESS, principal.getUserId());
        appendLog(orderId, current.getCode(), current.getCode(), RepairOperationTypeEnum.PROCESS,
            request.getProcessDesc(), principal.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long orderId, RepairSubmitRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        RepairStatusEnum current = requireStatus(order);
        RepairStateMachine.assertCanSubmit(current);
        assertMaintainer(order, principal.getUserId());

        order.setStatus(RepairStatusEnum.WAIT_CONFIRM.getCode());
        order.setFinishDesc(request.getFinishDesc());
        order.setFinishTime(LocalDateTime.now());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.updateSelective(order);

        saveAttachments(orderId, request.getAttachmentFileIds(), RepairAttachmentTypeEnum.RESULT, principal.getUserId());
        appendLog(orderId, current.getCode(), RepairStatusEnum.WAIT_CONFIRM.getCode(), RepairOperationTypeEnum.SUBMIT,
            request.getFinishDesc(), principal.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long orderId, RepairConfirmRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        assertOwner(order, principal.getUserId());
        RepairStatusEnum current = requireStatus(order);
        RepairStateMachine.assertCanConfirm(current);

        order.setStatus(RepairStatusEnum.DONE.getCode());
        order.setConfirmTime(LocalDateTime.now());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.updateSelective(order);

        appendLog(orderId, current.getCode(), RepairStatusEnum.DONE.getCode(), RepairOperationTypeEnum.CONFIRM,
            defaultRemark(request == null ? null : request.getRemark(), "居民确认已解决"), principal.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reopen(Long orderId, RepairReopenRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        assertOwner(order, principal.getUserId());
        RepairStatusEnum current = requireStatus(order);
        RepairStateMachine.assertCanReopen(current);

        order.setStatus(RepairStatusEnum.REOPENED.getCode());
        order.setResidentFeedback(request.getFeedback());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.updateSelective(order);

        saveAttachments(orderId, request.getAttachmentFileIds(), RepairAttachmentTypeEnum.FEEDBACK, principal.getUserId());
        appendLog(orderId, current.getCode(), RepairStatusEnum.REOPENED.getCode(), RepairOperationTypeEnum.REOPEN,
            request.getFeedback(), principal.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluate(Long orderId, RepairEvaluateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        assertOwner(order, principal.getUserId());
        RepairStatusEnum current = requireStatus(order);
        RepairStateMachine.assertCanEvaluate(current);

        order.setEvaluateScore(request.getScore());
        order.setEvaluateContent(request.getContent());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.updateSelective(order);

        appendLog(orderId, current.getCode(), current.getCode(), RepairOperationTypeEnum.EVALUATE,
            "评分: " + request.getScore(), principal.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void urge(Long orderId, RepairUrgeRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        assertOwner(order, principal.getUserId());
        RepairStatusEnum current = requireStatus(order);
        RepairStateMachine.assertCanUrge(current);

        int currentUrgeCount = order.getUrgeCount() == null ? 0 : order.getUrgeCount();
        order.setUrgeCount(currentUrgeCount + 1);
        order.setLastUrgeTime(LocalDateTime.now());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.updateSelective(order);

        appendLog(orderId, current.getCode(), current.getCode(), RepairOperationTypeEnum.URGE,
            request.getContent(), principal.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long orderId, RepairCloseRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        assertManageAccess(order, principal.getUserId());
        RepairStatusEnum current = requireStatus(order);
        RepairStateMachine.assertCanClose(current);

        order.setStatus(RepairStatusEnum.CLOSED.getCode());
        order.setClosedTime(LocalDateTime.now());
        order.setUpdateBy(principal.getUserId());
        bizRepairOrderMapper.updateSelective(order);

        appendLog(orderId, current.getCode(), RepairStatusEnum.CLOSED.getCode(), RepairOperationTypeEnum.CLOSE,
            defaultRemark(request == null ? null : request.getRemark(), "工单关闭"), principal.getUserId());
    }

    @Override
    public List<RepairOrderLogVO> flowLogs(Long orderId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizRepairOrder order = requireOrder(orderId);
        if (!principal.getUserId().equals(order.getResidentUserId())) {
            assertManageAccess(order, principal.getUserId());
        }
        return bizRepairOrderLogMapper.selectByRepairOrderId(orderId).stream().map(log -> RepairOrderLogVO.builder()
            .id(log.getId())
            .repairOrderId(log.getRepairOrderId())
            .fromStatus(log.getFromStatus())
            .toStatus(log.getToStatus())
            .operationType(log.getOperationType())
            .operatorUserId(log.getOperatorUserId())
            .operationRemark(log.getOperationRemark())
            .operationTime(log.getOperationTime())
            .build()).toList();
    }

    private BizRepairOrder requireOrder(Long orderId) {
        BizRepairOrder order = bizRepairOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "报修工单不存在");
        }
        return order;
    }

    private void assertOwner(BizRepairOrder order, Long userId) {
        if (!userId.equals(order.getResidentUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权操作他人报修工单");
        }
    }

    private void assertMaintainer(BizRepairOrder order, Long userId) {
        if (order.getMaintainerUserId() == null || !userId.equals(order.getMaintainerUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "仅分派维修员可执行当前操作");
        }
    }

    private void assertManageAccess(BizRepairOrder order, Long userId) {
        dataScopeService.assertOrgAccessible(userId, order.getComplexOrgId());
    }

    private RepairStatusEnum requireStatus(BizRepairOrder order) {
        RepairStatusEnum statusEnum = RepairStatusEnum.fromCode(order.getStatus());
        if (statusEnum == null) {
            throw new BizException(ErrorCode.ILLEGAL_STATE_FLOW, "工单状态非法");
        }
        return statusEnum;
    }

    private void appendLog(
        Long orderId,
        String fromStatus,
        String toStatus,
        RepairOperationTypeEnum operationType,
        String remark,
        Long operatorUserId
    ) {
        BizRepairOrderLog log = new BizRepairOrderLog();
        log.setRepairOrderId(orderId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperationType(operationType.getCode());
        log.setOperatorUserId(operatorUserId);
        log.setOperationRemark(remark);
        bizRepairOrderLogMapper.insert(log);
    }

    private void saveAttachments(Long orderId, Set<Long> fileIds, RepairAttachmentTypeEnum typeEnum, Long userId) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        Set<Long> distinctIds = new LinkedHashSet<>();
        for (Long fileId : fileIds) {
            if (fileId != null && fileId > 0) {
                distinctIds.add(fileId);
            }
        }
        if (distinctIds.isEmpty()) {
            return;
        }
        fileBindingService.assertFileIdsValid(distinctIds);
        List<BizRepairAttachment> list = new ArrayList<>();
        for (Long fileId : distinctIds) {
            BizRepairAttachment attachment = new BizRepairAttachment();
            attachment.setRepairOrderId(orderId);
            attachment.setFileId(fileId);
            attachment.setAttachmentType(typeEnum.getCode());
            attachment.setCreateBy(userId);
            list.add(attachment);
        }
        if (!list.isEmpty()) {
            bizRepairAttachmentMapper.batchInsert(list);
        }
    }

    private String generateOrderNo(Long userId) {
        return "RP" + LocalDateTime.now().format(ORDER_NO_FORMATTER) + String.format("%04d", userId % 10000);
    }

    private Long resolveCommunityOrgId(SysOrg complexOrg) {
        if (complexOrg.getParentId() == null) {
            return complexOrg.getId();
        }
        return complexOrg.getParentId();
    }

    private SysOrg requireComplexOrg(Long complexOrgId) {
        SysOrg complexOrg = sysOrgMapper.selectById(complexOrgId);
        if (complexOrg == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "报修所属小区不存在");
        }
        if (!OrgTypeEnum.COMPLEX.getCode().equalsIgnoreCase(complexOrg.getOrgType())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "报修所属组织必须为小区");
        }
        return complexOrg;
    }

    private Long resolvePropertyCompanyOrgId(Long requestPropertyCompanyOrgId, Long complexOrgId) {
        if (requestPropertyCompanyOrgId != null) {
            long relationCount = bizComplexPropertyRelMapper.countActiveByPropertyAndComplex(requestPropertyCompanyOrgId, complexOrgId);
            if (relationCount == 0) {
                throw new BizException(ErrorCode.BAD_REQUEST, "所选物业公司未服务该小区");
            }
            return requestPropertyCompanyOrgId;
        }
        List<BizComplexPropertyRel> relList = bizComplexPropertyRelMapper.selectByComplexOrgId(complexOrgId);
        return relList.stream()
            .filter(rel -> rel.getStatus() != null && rel.getStatus() == 1)
            .map(BizComplexPropertyRel::getPropertyCompanyOrgId)
            .findFirst()
            .orElseThrow(() -> new BizException(ErrorCode.BAD_REQUEST, "当前小区未配置服务物业公司"));
    }

    private void assertCreateOrgAccess(LoginPrincipal principal, Long complexOrgId) {
        if (principal.isSuperAdmin()) {
            return;
        }
        if (principal.getOrgId() != null && principal.getOrgId().equals(complexOrgId)) {
            return;
        }
        dataScopeService.assertOrgAccessible(principal.getUserId(), complexOrgId);
    }

    private SysUser requireEnabledUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "维修员不存在");
        }
        if (user.getStatus() == null || user.getStatus() != UserStatusEnum.ENABLED.getCode()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "维修员已禁用");
        }
        return user;
    }

    private RepairOrderVO toOrderVO(BizRepairOrder order, boolean withAttachments) {
        List<RepairAttachmentVO> attachments = List.of();
        if (withAttachments) {
            attachments = bizRepairAttachmentMapper.selectByRepairOrderId(order.getId()).stream().map(item -> RepairAttachmentVO.builder()
                .id(item.getId())
                .fileId(item.getFileId())
                .attachmentType(item.getAttachmentType())
                .createTime(item.getCreateTime())
                .build()).toList();
        }
        return RepairOrderVO.builder()
            .id(order.getId())
            .orderNo(order.getOrderNo())
            .title(order.getTitle())
            .description(order.getDescription())
            .contactPhone(order.getContactPhone())
            .repairAddress(order.getRepairAddress())
            .emergencyLevel(order.getEmergencyLevel())
            .expectHandleTime(order.getExpectHandleTime())
            .status(order.getStatus())
            .communityOrgId(order.getCommunityOrgId())
            .complexOrgId(order.getComplexOrgId())
            .propertyCompanyOrgId(order.getPropertyCompanyOrgId())
            .residentUserId(order.getResidentUserId())
            .acceptUserId(order.getAcceptUserId())
            .assignUserId(order.getAssignUserId())
            .maintainerUserId(order.getMaintainerUserId())
            .processDesc(order.getProcessDesc())
            .finishDesc(order.getFinishDesc())
            .rejectReason(order.getRejectReason())
            .residentFeedback(order.getResidentFeedback())
            .evaluateScore(order.getEvaluateScore())
            .evaluateContent(order.getEvaluateContent())
            .urgeCount(order.getUrgeCount())
            .acceptedTime(order.getAcceptedTime())
            .assignedTime(order.getAssignedTime())
            .processingTime(order.getProcessingTime())
            .finishTime(order.getFinishTime())
            .confirmTime(order.getConfirmTime())
            .closedTime(order.getClosedTime())
            .lastUrgeTime(order.getLastUrgeTime())
            .createTime(order.getCreateTime())
            .attachments(attachments)
            .build();
    }

    private String defaultRemark(String rawRemark, String defaultRemark) {
        if (StringUtils.isBlank(rawRemark)) {
            return defaultRemark;
        }
        return rawRemark;
    }

}
