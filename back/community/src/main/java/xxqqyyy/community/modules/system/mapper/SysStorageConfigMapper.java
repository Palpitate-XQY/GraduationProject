package xxqqyyy.community.modules.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.system.entity.SysStorageConfig;

/**
 * 存储配置 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface SysStorageConfigMapper {

    SysStorageConfig selectCurrent();

    int insert(SysStorageConfig config);

    int update(SysStorageConfig config);

    int countAll();

    int logicalDelete(@Param("id") Long id, @Param("updateBy") Long updateBy);
}

