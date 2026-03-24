package xxqqyyy.community.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import xxqqyyy.community.security.JwtProperties;

/**
 * JWT 签发与解析服务。
 *
 * @author codex
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    /**
     * 签发 token。
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return token 字符串
     */
    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(jwtProperties.getExpireMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .issuer(jwtProperties.getIssuer())
            .subject(String.valueOf(userId))
            .claim("username", username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expireAt))
            .signWith(signingKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * 解析 token。
     *
     * @param token token
     * @return 声明体
     */
    public Jws<Claims> parse(String token) {
        return Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token);
    }

    /**
     * 计算 token 剩余秒数。
     *
     * @param claims 声明体
     * @return 剩余秒数
     */
    public long remainSeconds(Claims claims) {
        long diff = claims.getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(0, diff / 1000);
    }

    private SecretKey signingKey() {
        byte[] secretBytes = jwtProperties.getSecret() == null ? new byte[0] : jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length >= 32) {
            return Keys.hmacShaKeyFor(secretBytes);
        }
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode("Q29tbXVuaXR5U2VydmljZVN5c3RlbVNlY3JldEtleTIwMjYxMjM0NQ=="));
    }
}
