package xxqqyyy.community.modules.system.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.system.entity.SysPermission;

/**
 * 权限 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface SysPermissionMapper {

    SysPermission selectById(@Param("id") Long id);

    SysPermission selectByCode(@Param("permissionCode") String permissionCode);

    long countByCode(@Param("permissionCode") String permissionCode, @Param("excludeId") Long excludeId);

    int insert(SysPermission permission);

    int update(SysPermission permission);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    List<SysPermission> selectPage(
        @Param("keyword") String keyword,
        @Param("offset") long offset,
        @Param("size") long size
    );

    long countPage(@Param("keyword") String keyword);

    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    List<String> selectCodesByIds(@Param("ids") Collection<Long> ids);
}

