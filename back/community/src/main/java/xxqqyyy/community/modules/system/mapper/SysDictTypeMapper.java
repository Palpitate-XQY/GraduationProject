package xxqqyyy.community.modules.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.system.dto.DictTypePageQuery;
import xxqqyyy.community.modules.system.entity.SysDictType;

/**
 * 字典类型 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface SysDictTypeMapper {

    SysDictType selectById(@Param("id") Long id);

    SysDictType selectByCode(@Param("dictCode") String dictCode);

    long countByCode(@Param("dictCode") String dictCode, @Param("excludeId") Long excludeId);

    int insert(SysDictType dictType);

    int update(SysDictType dictType);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    long countPage(@Param("query") DictTypePageQuery query);

    List<SysDictType> selectPage(
        @Param("query") DictTypePageQuery query,
        @Param("offset") long offset,
        @Param("size") long size
    );
}
