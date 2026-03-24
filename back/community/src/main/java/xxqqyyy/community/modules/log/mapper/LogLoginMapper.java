package xxqqyyy.community.modules.log.mapper;

import org.apache.ibatis.annotations.Mapper;
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
}

