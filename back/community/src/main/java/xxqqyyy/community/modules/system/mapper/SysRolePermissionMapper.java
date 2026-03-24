package xxqqyyy.community.modules.system.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色权限关联 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface SysRolePermissionMapper {

    int deleteByRoleId(@Param("roleId") Long roleId);

    int batchInsert(@Param("roleId") Long roleId, @Param("permissionIds") Collection<Long> permissionIds);

    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
}

