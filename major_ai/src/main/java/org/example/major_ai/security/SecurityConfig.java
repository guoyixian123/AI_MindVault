package org.example.major_ai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security配置类
 * 配置认证、授权、CORS、异常处理等安全策略
 *
 * 核心配置：
 * 1. 禁用CSRF（前后端分离，使用JWT）
 * 2. 启用CORS（允许跨域）
 * 3. 无状态会话（不使用Session）
 * 4. JWT过滤器（验证Token）
 * 5. 权限控制（白名单、角色限制）
 */
@Configuration
@EnableWebSecurity      // 启用Web安全
@EnableMethodSecurity   // 启用方法级安全（@PreAuthorize等）
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWT认证过滤器 */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** JSON序列化器 */
    private final ObjectMapper objectMapper;

    /**
     * 配置安全过滤链
     *
     * @param http HttpSecurity对象
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. 禁用CSRF（前后端分离，使用JWT认证，不需要CSRF）
            .csrf(AbstractHttpConfigurer::disable)

            // 2. 启用CORS（使用自定义的CORS配置）
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 3. 无状态会话（不使用HttpSession，使用JWT）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 4. 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 白名单路径（无需认证）
                .requestMatchers("/api/auth/**").permitAll()           // 登录/注册接口
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // OPTIONS预检请求

                // 静态资源
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/assets/**", "/favicon.ico").permitAll()

                // AI聊天接口（保留原有兼容）
                .requestMatchers(HttpMethod.POST, "/api/chat").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/chat/**").permitAll()

                // WebSocket接口
                .requestMatchers("/ws/**").permitAll()

                // 科室列表公开访问
                .requestMatchers(HttpMethod.GET, "/api/admin/departments").permitAll()

                // 管理端接口需要ROOT_ADMIN或DOCTOR角色
                .requestMatchers("/api/admin/**").hasAnyRole("ROOT_ADMIN", "DOCTOR")

                // 其他API需要认证
                .requestMatchers("/api/**").authenticated()

                // 其他请求放行
                .anyRequest().permitAll()
            )

            // 5. 配置异常处理
            .exceptionHandling(ex -> ex
                // 未认证（401）
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(
                            ApiResponse.error(401, "未登录或登录已过期，请重新登录")
                    ));
                })
                // 权限不足（403）
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(
                            ApiResponse.error(403, "权限不足，无法访问该资源")
                    ));
                })
            )

            // 6. 添加JWT过滤器（在UsernamePasswordAuthenticationFilter之前）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置CORS（跨域资源共享）
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));  // 允许所有来源
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));  // 允许的HTTP方法
        configuration.setAllowedHeaders(List.of("*"));  // 允许所有请求头
        configuration.setAllowCredentials(true);  // 允许携带凭证（Cookie）
        configuration.setMaxAge(3600L);  // 预检请求缓存时间（秒）

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 应用到所有路径
        return source;
    }

    /**
     * 配置认证管理器
     *
     * @param config AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置密码编码器
     * 使用BCrypt算法加密密码
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
