package xxqqyyy.community.modules.log.service;

import xxqqyyy.community.common.api.PageResult;
import xxqqyyy.community.modules.log.dto.LoginLogPageQuery;
import xxqqyyy.community.modules.log.vo.LoginLogVO;

/**
 * 登录日志服务接口。
 *
 * @author codex
 * @since 1.0.0
 */
public interface LoginLogService {

    /**
     * 记录登录结果。
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param success 是否成功
     * @param message 登录消息
     * @param ip 请求IP
     * @param userAgent 浏览器信息
     */
    void record(Long userId, String username, boolean success, String message, String ip, String userAgent);

    /**
     * 登录日志分页查询。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<LoginLogVO> page(LoginLogPageQuery query);
}
