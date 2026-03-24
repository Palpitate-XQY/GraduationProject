package xxqqyyy.community.modules.log.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xxqqyyy.community.modules.log.dto.LoginLogPageQuery;
import xxqqyyy.community.modules.log.entity.LogLogin;

/**
 * 登录日志 Mapper。
 *
 * @author codex
 * @since 1.0.0
 */
@Mapper
public interface LogLoginMapper {

    /**
     * 插入登录日志。
     *
     * @param logLogin 日志对象
     * @return 影响行数
     */
    int insert(LogLogin logLogin);

    long countPage(@Param("query") LoginLogPageQuery query);

    List<LogLogin> selectPage(
        @Param("query") LoginLogPageQuery query,
        @Param("offset") long offset,
        @Param("size") long size
    );
}
