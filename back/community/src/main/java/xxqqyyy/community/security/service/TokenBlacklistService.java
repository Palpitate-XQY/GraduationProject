package xxqqyyy.community.security.service;

/**
 * Token 黑名单服务接口。
 *
 * @author codex
 * @since 1.0.0
 */
public interface TokenBlacklistService {

    /**
     * 将 tokenId 加入黑名单。
     *
     * @param tokenId         token 唯一标识
     * @param expireSeconds   过期秒数
     */
    void blacklist(String tokenId, long expireSeconds);

    /**
     * 判断 tokenId 是否在黑名单中。
     *
     * @param tokenId token 唯一标识
     * @return 是否在黑名单
     */
    boolean isBlacklisted(String tokenId);
}

