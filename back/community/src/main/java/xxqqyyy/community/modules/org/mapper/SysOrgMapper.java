package xxqqyyy.community.modules.org.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.org.dto.OrgQuery;
import xxqqyyy.community.modules.org.entity.SysOrg;

/**
 * 组织 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface SysOrgMapper {

    SysOrg selectById(@Param("id") Long id);

    SysOrg selectByCode(@Param("orgCode") String orgCode);

    long countByCode(@Param("orgCode") String orgCode, @Param("excludeId") Long excludeId);

    int insert(SysOrg org);

    int update(SysOrg org);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    List<SysOrg> selectByIds(@Param("ids") Collection<Long> ids);

    List<Long> selectDescendantIds(@Param("rootId") Long rootId);

    List<SysOrg> selectTree(
        @Param("query") OrgQuery query,
        @Param("allAccess") boolean allAccess,
        @Param("orgIds") Collection<Long> orgIds
    );
}

