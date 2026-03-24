package xxqqyyy.community.modules.system.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.system.entity.SysRoleScope;

/**
 * 角色数据范围 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface SysRoleScopeMapper {

    int deleteByRoleId(@Param("roleId") Long roleId);

    int batchInsert(@Param("scopes") Collection<SysRoleScope> scopes);

    List<SysRoleScope> selectByRoleId(@Param("roleId") Long roleId);

    List<SysRoleScope> selectByRoleIds(@Param("roleIds") Collection<Long> roleIds);
}

