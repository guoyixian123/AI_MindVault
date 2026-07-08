package org.example.major_ai.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 用户详情实现类
 * 实现Spring Security的UserDetails接口，用于认证和授权
 *
 * 与UserEntity的区别：
 * - UserEntity：数据库实体，包含所有用户字段
 * - UserDetailsImpl：安全实体，只包含认证和授权相关字段
 *
 * 使用场景：
 * - JwtAuthenticationFilter：从Token创建UserDetailsImpl
 * - @AuthenticationPrincipal：在Controller中获取当前用户
 */
@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（JWT认证时为空） */
    private String password;

    /** 角色（ROOT_ADMIN/DOCTOR/USER） */
    private String role;

    /**
     * 获取用户权限列表
     * 将角色转换为Spring Security的权限格式
     *
     * @return 权限列表
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将角色转换为权限（如：USER -> ROLE_USER）
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 账户是否未过期
     * 本项目不使用账户过期功能，始终返回true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户是否未锁定
     * 本项目不使用账户锁定功能，始终返回true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证是否未过期
     * 本项目不使用凭证过期功能，始终返回true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否启用
     * 本项目不使用账户禁用功能，始终返回true
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
