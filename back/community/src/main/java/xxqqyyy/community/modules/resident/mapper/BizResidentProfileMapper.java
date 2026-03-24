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

    /**
     * 根据用户ID查询居民档案。
     *
     * @param userId 用户ID
     * @return 居民档案
     */
    BizResidentProfile selectByUserId(@Param("userId") Long userId);

    /**
     * 新增居民档案。
     *
     * @param profile 档案对象
     * @return 影响行数
     */
    int insert(BizResidentProfile profile);
}
