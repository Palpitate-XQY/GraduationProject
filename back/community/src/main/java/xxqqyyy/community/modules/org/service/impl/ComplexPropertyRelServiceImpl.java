package xxqqyyy.community.modules.org.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xxqqyyy.community.modules.org.dto.ComplexPropertyRelCreateRequest;
import xxqqyyy.community.modules.org.entity.BizComplexPropertyRel;
import xxqqyyy.community.modules.org.mapper.BizComplexPropertyRelMapper;
import xxqqyyy.community.modules.org.service.ComplexPropertyRelService;
import xxqqyyy.community.modules.org.vo.ComplexPropertyRelVO;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 小区物业关系服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ComplexPropertyRelServiceImpl implements ComplexPropertyRelService {

    private final BizComplexPropertyRelMapper relMapper;
    private final DataScopeService dataScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ComplexPropertyRelCreateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        dataScopeService.assertOrgAccessible(principal.getUserId(), request.getComplexOrgId());
        dataScopeService.assertOrgAccessible(principal.getUserId(), request.getPropertyCompanyOrgId());
        BizComplexPropertyRel rel = new BizComplexPropertyRel();
        rel.setComplexOrgId(request.getComplexOrgId());
        rel.setPropertyCompanyOrgId(request.getPropertyCompanyOrgId());
        rel.setStatus(1);
        rel.setCreateBy(principal.getUserId());
        rel.setUpdateBy(principal.getUserId());
        relMapper.insert(rel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long relId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        relMapper.logicalDelete(relId, principal.getUserId());
    }

    @Override
    public List<ComplexPropertyRelVO> listByComplex(Long complexOrgId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        dataScopeService.assertOrgAccessible(principal.getUserId(), complexOrgId);
        return relMapper.selectByComplexOrgId(complexOrgId).stream().map(rel -> ComplexPropertyRelVO.builder()
            .id(rel.getId())
            .complexOrgId(rel.getComplexOrgId())
            .propertyCompanyOrgId(rel.getPropertyCompanyOrgId())
            .status(rel.getStatus())
            .build()).toList();
    }
}

