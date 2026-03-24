package xxqqyyy.community.modules.system.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色关联 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface SysUserRoleMapper {

    int deleteByUserId(@Param("userId") Long userId);

    int batchInsert(@Param("userId") Long userId, @Param("roleIds") Collection<Long> roleIds);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}

