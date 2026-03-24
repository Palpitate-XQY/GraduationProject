package xxqqyyy.community.modules.resident.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.resident.entity.BizResidentProfile;

/**
 * 居民档案 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface BizResidentProfileMapper {

    BizResidentProfile selectByUserId(@Param("userId") Long userId);

    int insert(BizResidentProfile profile);

    int updateByUserId(BizResidentProfile profile);
}
