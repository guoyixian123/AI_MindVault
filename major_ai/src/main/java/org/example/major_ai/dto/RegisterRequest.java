package org.example.major_ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求DTO
 * 用于用户注册接口的请求参数
 *
 * 请求示例：
 * {
 *   "username": "newuser",
 *   "password": "123456",
 *   "nickname": "新用户",
 *   "phone": "13800138000",
 *   "email": "user@example.com"
 * }
 */
@Data
public class RegisterRequest {

    /** 用户名（必填，3-50个字符） */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50个字符")
    private String username;

    /** 密码（必填，6-100个字符） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度6-100个字符")
    private String password;

    /** 昵称（可选） */
    private String nickname;

    /** 手机号（可选） */
    private String phone;

    /** 邮箱（可选） */
    private String email;
}
