package xxqqyyy.community.modules.system.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.system.dto.UserPageQuery;
import xxqqyyy.community.modules.system.entity.SysUser;

/**
 * 用户表 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface SysUserMapper {

    SysUser selectById(@Param("id") Long id);

    SysUser selectByUsername(@Param("username") String username);

    long countByUsername(@Param("username") String username, @Param("excludeId") Long excludeId);

    int insert(SysUser user);

    int update(SysUser user);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateBy") Long updateBy);

    int updatePassword(
        @Param("id") Long id,
        @Param("password") String password,
        @Param("mustChangePassword") Integer mustChangePassword,
        @Param("updateBy") Long updateBy
    );

    long countPage(
        @Param("query") UserPageQuery query,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds
    );

    List<SysUser> selectPage(
        @Param("query") UserPageQuery query,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds,
        @Param("offset") long offset,
        @Param("size") long size
    );
}

