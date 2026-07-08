package org.example.major_ai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT认证过滤器
 * 拦截每个请求，验证JWT Token，设置认证信息
 *
 * 工作流程：
 * 1. 从请求头提取Authorization字段
 * 2. 验证Token是否有效
 * 3. 从Token中提取用户信息
 * 4. 创建认证对象，设置到SecurityContext
 * 5. 继续执行后续过滤器
 *
 * 请求头格式：Authorization: Bearer {token}
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT工具类，负责Token的验证和解析 */
    private final JwtUtil jwtUtil;

    /**
     * 过滤器核心方法
     * 每个请求都会执行一次
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 从请求头提取Authorization字段
        String header = request.getHeader("Authorization");

        // 2. 检查是否包含Bearer Token
        if (header != null && header.startsWith("Bearer ")) {
            // 提取Token（去掉"Bearer "前缀）
            String token = header.substring(7);

            // 3. 验证Token是否有效
            if (jwtUtil.validateToken(token)) {
                // 4. 从Token中提取用户信息
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                // 5. 创建UserDetails对象
                UserDetailsImpl userDetails = new UserDetailsImpl(userId, username, "", role);

                // 6. 创建认证对象
                // 参数：principal（用户信息）、credentials（密码，JWT不需要）、authorities（权限列表）
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))  // 添加角色权限
                        );

                // 7. 设置认证信息到SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 8. 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }
}
