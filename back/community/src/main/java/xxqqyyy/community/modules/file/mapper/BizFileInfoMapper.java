package xxqqyyy.community.modules.file.mapper;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.file.entity.BizFileInfo;

/**
 * 文件信息 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface BizFileInfoMapper {

    int insert(BizFileInfo fileInfo);

    BizFileInfo selectById(@Param("id") Long id);

    List<BizFileInfo> selectByIds(@Param("ids") Collection<Long> ids);

    int updateBizBind(
        @Param("id") Long id,
        @Param("bizType") String bizType,
        @Param("bizId") Long bizId,
        @Param("updateBy") Long updateBy
    );
}

