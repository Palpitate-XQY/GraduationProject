package xxqqyyy.community.modules.resident.service.impl;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.org.entity.SysOrg;
import xxqqyyy.community.modules.org.enums.OrgTypeEnum;
import xxqqyyy.community.modules.org.mapper.SysOrgMapper;
import xxqqyyy.community.modules.resident.dto.ResidentProfileAdminUpdateRequest;
import xxqqyyy.community.modules.resident.dto.ResidentProfileMyUpdateRequest;
import xxqqyyy.community.modules.resident.entity.BizResidentProfile;
import xxqqyyy.community.modules.resident.mapper.BizResidentProfileMapper;
import xxqqyyy.community.modules.resident.service.ResidentProfileService;
import xxqqyyy.community.modules.resident.vo.ResidentProfileVO;
import xxqqyyy.community.modules.system.entity.SysUser;
import xxqqyyy.community.modules.system.mapper.SysUserMapper;
import xxqqyyy.community.modules.system.service.DataScopeService;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 居民档案服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ResidentProfileServiceImpl implements ResidentProfileService {

    private final BizResidentProfileMapper bizResidentProfileMapper;
    private final SysUserMapper sysUserMapper;
    private final SysOrgMapper sysOrgMapper;
    private final DataScopeService dataScopeService;

    @Override
    public ResidentProfileVO getMyProfile() {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        BizResidentProfile profile = requireProfile(principal.getUserId());
        return toVO(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyProfile(ResidentProfileMyUpdateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysUser user = requireUser(principal.getUserId());
        SysOrg complexOrg = requireEnabledComplexOrg(user.getOrgId());
        SysOrg communityOrg = resolveCommunityOrg(complexOrg);

        BizResidentProfile profile = bizResidentProfileMapper.selectByUserId(principal.getUserId());
        if (profile == null) {
            BizResidentProfile insertProfile = new BizResidentProfile();
            insertProfile.setUserId(principal.getUserId());
            insertProfile.setCommunityOrgId(communityOrg.getId());
            insertProfile.setComplexOrgId(complexOrg.getId());
            insertProfile.setRoomNo(request.getRoomNo());
            insertProfile.setEmergencyContact(request.getEmergencyContact());
            insertProfile.setEmergencyPhone(request.getEmergencyPhone());
            insertProfile.setCreateBy(principal.getUserId());
            insertProfile.setUpdateBy(principal.getUserId());
            bizResidentProfileMapper.insert(insertProfile);
            return;
        }

        profile.setCommunityOrgId(communityOrg.getId());
        profile.setComplexOrgId(complexOrg.getId());
        profile.setRoomNo(request.getRoomNo());
        profile.setEmergencyContact(request.getEmergencyContact());
        profile.setEmergencyPhone(request.getEmergencyPhone());
        profile.setUpdateBy(principal.getUserId());
        bizResidentProfileMapper.updateByUserId(profile);
    }

    @Override
    public ResidentProfileVO getByUserId(Long userId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysUser user = requireUser(userId);
        dataScopeService.assertOrgAccessible(principal.getUserId(), user.getOrgId());
        BizResidentProfile profile = requireProfile(userId);
        return toVO(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertByAdmin(ResidentProfileAdminUpdateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysUser residentUser = requireUser(request.getUserId());
        dataScopeService.assertOrgAccessible(principal.getUserId(), residentUser.getOrgId());

        SysOrg complexOrg = requireEnabledComplexOrg(request.getComplexOrgId());
        dataScopeService.assertOrgAccessible(principal.getUserId(), complexOrg.getId());
        SysOrg communityOrg = resolveCommunityOrg(complexOrg);

        BizResidentProfile profile = bizResidentProfileMapper.selectByUserId(request.getUserId());
        if (profile == null) {
            BizResidentProfile insertProfile = new BizResidentProfile();
            insertProfile.setUserId(request.getUserId());
            insertProfile.setCommunityOrgId(communityOrg.getId());
            insertProfile.setComplexOrgId(complexOrg.getId());
            insertProfile.setRoomNo(request.getRoomNo());
            insertProfile.setEmergencyContact(request.getEmergencyContact());
            insertProfile.setEmergencyPhone(request.getEmergencyPhone());
            insertProfile.setCreateBy(principal.getUserId());
            insertProfile.setUpdateBy(principal.getUserId());
            bizResidentProfileMapper.insert(insertProfile);
        } else {
            profile.setCommunityOrgId(communityOrg.getId());
            profile.setComplexOrgId(complexOrg.getId());
            profile.setRoomNo(request.getRoomNo());
            profile.setEmergencyContact(request.getEmergencyContact());
            profile.setEmergencyPhone(request.getEmergencyPhone());
            profile.setUpdateBy(principal.getUserId());
            bizResidentProfileMapper.updateByUserId(profile);
        }

        if (!Objects.equals(residentUser.getOrgId(), complexOrg.getId())) {
            sysUserMapper.updateOrgId(request.getUserId(), complexOrg.getId(), principal.getUserId());
        }
    }

    private BizResidentProfile requireProfile(Long userId) {
        BizResidentProfile profile = bizResidentProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "居民档案不存在");
        }
        return profile;
    }

    private SysUser requireUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private SysOrg requireEnabledComplexOrg(Long complexOrgId) {
        SysOrg org = sysOrgMapper.selectById(complexOrgId);
        if (org == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "小区组织不存在");
        }
        if (!OrgTypeEnum.COMPLEX.getCode().equalsIgnoreCase(org.getOrgType())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "目标组织必须为小区类型");
        }
        if (org.getStatus() == null || org.getStatus() != 1) {
            throw new BizException(ErrorCode.BIZ_ERROR, "小区组织已停用");
        }
        return org;
    }

    private SysOrg resolveCommunityOrg(SysOrg complexOrg) {
        if (complexOrg.getParentId() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "小区未配置所属社区");
        }
        SysOrg communityOrg = sysOrgMapper.selectById(complexOrg.getParentId());
        if (communityOrg == null || !OrgTypeEnum.COMMUNITY.getCode().equalsIgnoreCase(communityOrg.getOrgType())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "小区所属社区配置非法");
        }
        return communityOrg;
    }

    private ResidentProfileVO toVO(BizResidentProfile profile) {
        SysOrg communityOrg = sysOrgMapper.selectById(profile.getCommunityOrgId());
        SysOrg complexOrg = sysOrgMapper.selectById(profile.getComplexOrgId());
        return ResidentProfileVO.builder()
            .id(profile.getId())
            .userId(profile.getUserId())
            .communityOrgId(profile.getCommunityOrgId())
            .communityOrgName(communityOrg == null ? null : communityOrg.getOrgName())
            .complexOrgId(profile.getComplexOrgId())
            .complexOrgName(complexOrg == null ? null : complexOrg.getOrgName())
            .roomNo(profile.getRoomNo())
            .emergencyContact(profile.getEmergencyContact())
            .emergencyPhone(profile.getEmergencyPhone())
            .updateTime(profile.getUpdateTime())
            .build();
    }
}
