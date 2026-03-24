package xxqqyyy.community.modules.system.mapper;

import java.util.List;
import java.util.Collection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.system.entity.SysRole;

/**
 * 角色 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface SysRoleMapper {

    SysRole selectById(@Param("id") Long id);

    SysRole selectByCode(@Param("roleCode") String roleCode);

    long countByCode(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);

    int insert(SysRole role);

    int update(SysRole role);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    List<SysRole> selectEnabledByUserId(@Param("userId") Long userId);

    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    List<SysRole> selectByIds(@Param("ids") Collection<Long> ids);

    List<SysRole> selectPage(
        @Param("keyword") String keyword,
        @Param("offset") long offset,
        @Param("size") long size
    );

    long countPage(@Param("keyword") String keyword);
}
