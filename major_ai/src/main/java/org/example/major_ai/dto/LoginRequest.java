package org.example.major_ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 * 用于用户登录接口的请求参数
 *
 * 请求示例：
 * {
 *   "username": "admin",
 *   "password": "123456"
 * }
 */
@Data
public class LoginRequest {

    /** 用户名（必填） */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码（必填） */
    @NotBlank(message = "密码不能为空")
    private String password;
}
