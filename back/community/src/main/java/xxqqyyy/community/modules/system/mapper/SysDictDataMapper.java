package xxqqyyy.community.modules.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.system.dto.DictDataPageQuery;
import xxqqyyy.community.modules.system.entity.SysDictData;

/**
 * 字典数据 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface SysDictDataMapper {

    SysDictData selectById(@Param("id") Long id);

    long countByTypeAndValue(
        @Param("dictTypeCode") String dictTypeCode,
        @Param("dictValue") String dictValue,
        @Param("excludeId") Long excludeId
    );

    int insert(SysDictData dictData);

    int update(SysDictData dictData);

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);

    long countByTypeCode(@Param("dictTypeCode") String dictTypeCode);

    long countPage(@Param("query") DictDataPageQuery query);

    List<SysDictData> selectPage(
        @Param("query") DictDataPageQuery query,
        @Param("offset") long offset,
        @Param("size") long size
    );

    List<SysDictData> selectOptionsByTypeCode(@Param("dictTypeCode") String dictTypeCode);
}
