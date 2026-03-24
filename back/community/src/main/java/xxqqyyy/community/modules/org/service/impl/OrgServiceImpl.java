package xxqqyyy.community.modules.org.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.org.dto.OrgCreateRequest;
import xxqqyyy.community.modules.org.dto.OrgQuery;
import xxqqyyy.community.modules.org.dto.OrgUpdateRequest;
import xxqqyyy.community.modules.org.entity.SysOrg;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.org.service.OrgService;
import xxqqyyy.community.modules.org.vo.OrgTreeVO;
import xxqqyyy.community.modules.system.model.DataScopeResult;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 组织管理服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class OrgServiceImpl implements OrgService {

    private final SysOrgMapper sysOrgMapper;
    private final DataScopeService dataScopeService;

    @Override
    public List<OrgTreeVO> tree(OrgQuery query) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        DataScopeResult scope = dataScopeService.resolveByUserId(principal.getUserId());
        List<SysOrg> orgList = sysOrgMapper.selectTree(query, scope.isAllAccess(), scope.getSafeOrgIds());
        return buildTree(orgList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(OrgCreateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        if (sysOrgMapper.countByCode(request.getOrgCode(), null) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "组织编码已存在");
        }
        if (request.getParentId() != null) {
            dataScopeService.assertOrgAccessible(principal.getUserId(), request.getParentId());
        }
        SysOrg org = new SysOrg();
        org.setParentId(request.getParentId());
        org.setOrgCode(request.getOrgCode());
        org.setOrgName(request.getOrgName());
        org.setOrgType(request.getOrgType());
        org.setStatus(request.getStatus());
        org.setSort(request.getSort());
        org.setAncestorPath(buildAncestorPath(request.getParentId()));
        org.setCreateBy(principal.getUserId());
        org.setUpdateBy(principal.getUserId());
        sysOrgMapper.insert(org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OrgUpdateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysOrg org = requireOrg(request.getId());
        dataScopeService.assertOrgAccessible(principal.getUserId(), org.getId());
        if (request.getParentId() != null) {
            dataScopeService.assertOrgAccessible(principal.getUserId(), request.getParentId());
        }
        org.setParentId(request.getParentId());
        org.setOrgName(request.getOrgName());
        org.setOrgType(request.getOrgType());
        org.setStatus(request.getStatus());
        org.setSort(request.getSort());
        org.setAncestorPath(buildAncestorPath(request.getParentId()));
        org.setUpdateBy(principal.getUserId());
        sysOrgMapper.update(org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long orgId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        dataScopeService.assertOrgAccessible(principal.getUserId(), orgId);
        requireOrg(orgId);
        sysOrgMapper.logicalDelete(orgId, principal.getUserId());
    }

    private String buildAncestorPath(Long parentId) {
        if (parentId == null) {
            return "/";
        }
        SysOrg parent = requireOrg(parentId);
        return parent.getAncestorPath() + parent.getId() + "/";
    }

    private SysOrg requireOrg(Long orgId) {
        SysOrg org = sysOrgMapper.selectById(orgId);
        if (org == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "组织不存在");
        }
        return org;
    }

    private List<OrgTreeVO> buildTree(List<SysOrg> orgList) {
        Map<Long, OrgTreeVO> nodeMap = new LinkedHashMap<>();
        for (SysOrg org : orgList) {
            nodeMap.put(org.getId(), OrgTreeVO.builder()
                .id(org.getId())
                .parentId(org.getParentId())
                .orgCode(org.getOrgCode())
                .orgName(org.getOrgName())
                .orgType(org.getOrgType())
                .status(org.getStatus())
                .sort(org.getSort())
                .children(new ArrayList<>())
                .build());
        }
        List<OrgTreeVO> roots = new ArrayList<>();
        for (OrgTreeVO node : nodeMap.values()) {
            if (node.getParentId() == null || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
                continue;
            }
            nodeMap.get(node.getParentId()).getChildren().add(node);
        }
        return roots;
    }
}

