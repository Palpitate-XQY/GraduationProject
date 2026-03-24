package xxqqyyy.community.modules.system.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.common.enums.ErrorCode;
import xxqqyyy.community.common.exception.BizException;
import xxqqyyy.community.modules.system.dto.PermissionCreateRequest;
import xxqqyyy.community.modules.system.dto.PermissionUpdateRequest;
import xxqqyyy.community.modules.system.entity.SysPermission;
import xxqqyyy.community.modules.system.mapper.SysPermissionMapper;
import xxqqyyy.community.modules.system.service.PermissionService;
import xxqqyyy.community.modules.system.vo.PermissionVO;
import xxqqyyy.community.security.SecurityContextHelper;
import xxqqyyy.community.security.model.LoginPrincipal;

/**
 * 权限管理服务实现。
 *
 * @author codex
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public PageResult<PermissionVO> page(String keyword, long current, long size) {
        long total = sysPermissionMapper.countPage(keyword);
        if (total == 0) {
            return PageResult.empty(current, size);
        }
        long offset = (current - 1) * size;
        List<PermissionVO> records = sysPermissionMapper.selectPage(keyword, offset, size).stream()
            .map(this::toVO)
            .toList();
        return PageResult.<PermissionVO>builder()
            .records(records)
            .total(total)
            .current(current)
            .size(size)
            .build();
    }

    @Override
    public PermissionVO getById(Long permissionId) {
        return toVO(requirePermission(permissionId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(PermissionCreateRequest request) {
        if (sysPermissionMapper.countByCode(request.getPermissionCode(), null) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "权限编码已存在");
        }
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysPermission permission = new SysPermission();
        permission.setPermissionCode(request.getPermissionCode());
        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionType(request.getPermissionType());
        permission.setParentId(request.getParentId());
        permission.setPath(request.getPath());
        permission.setMethod(request.getMethod());
        permission.setSort(request.getSort() == null ? 0 : request.getSort());
        permission.setCreateBy(principal.getUserId());
        permission.setUpdateBy(principal.getUserId());
        sysPermissionMapper.insert(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PermissionUpdateRequest request) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        SysPermission permission = requirePermission(request.getId());
        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionType(request.getPermissionType());
        permission.setParentId(request.getParentId());
        permission.setPath(request.getPath());
        permission.setMethod(request.getMethod());
        permission.setSort(request.getSort() == null ? 0 : request.getSort());
        permission.setUpdateBy(principal.getUserId());
        sysPermissionMapper.update(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long permissionId) {
        LoginPrincipal principal = SecurityContextHelper.getCurrentPrincipal();
        requirePermission(permissionId);
        sysPermissionMapper.logicalDelete(permissionId, principal.getUserId());
    }

    private SysPermission requirePermission(Long permissionId) {
        SysPermission permission = sysPermissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "权限不存在");
        }
        return permission;
    }

    private PermissionVO toVO(SysPermission permission) {
        return PermissionVO.builder()
            .id(permission.getId())
            .permissionCode(permission.getPermissionCode())
            .permissionName(permission.getPermissionName())
            .permissionType(permission.getPermissionType())
            .parentId(permission.getParentId())
            .path(permission.getPath())
            .method(permission.getMethod())
            .sort(permission.getSort())
            .createTime(permission.getCreateTime())
            .build();
    }
}

