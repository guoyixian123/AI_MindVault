package org.example.major_ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应DTO
 * 用于登录、注册和获取当前用户接口的响应数据
 *
 * 响应示例：
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "userId": 1,
 *   "username": "admin",
 *   "nickname": "管理员",
 *   "role": "ROOT_ADMIN",
 *   "departmentId": null
 * }
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /** JWT Token（用于后续请求认证） */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 角色（ROOT_ADMIN/DOCTOR/USER） */
    private String role;

    /** 科室ID（仅医生角色有值） */
    private Long departmentId;
}
