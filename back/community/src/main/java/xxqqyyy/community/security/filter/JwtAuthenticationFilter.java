package xxqqyyy.community.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import xxqqyyy.community.security.model.LoginPrincipal;
import xxqqyyy.community.security.service.JwtTokenService;
import xxqqyyy.community.security.service.SecurityPrincipalService;
import xxqqyyy.community.security.service.TokenBlacklistService;

/**
 * JWT 鉴权过滤器，负责解析 token 并写入 SecurityContext。
 *
 * @author codex
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SecurityPrincipalService securityPrincipalService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        try {
            Jws<Claims> claimsJws = jwtTokenService.parse(token);
            Claims claims = claimsJws.getPayload();
            String tokenId = claims.getId();
            if (tokenBlacklistService.isBlacklisted(tokenId)) {
                filterChain.doFilter(request, response);
                return;
            }
            Long userId = Long.parseLong(claims.getSubject());
            LoginPrincipal principal = securityPrincipalService.loadByUserId(userId);
            if (principal != null) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.toAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception ex) {
            log.debug("JWT 解析失败: {}", ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}

