package org.example.major_ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.dto.LoginRequest;
import org.example.major_ai.dto.LoginResponse;
import org.example.major_ai.dto.RegisterRequest;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.UserEntity;
import org.example.major_ai.security.JwtUtil;
import org.example.major_ai.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供用户登录、注册和获取当前用户信息的接口
 *
 * API前缀：/api/auth
 *
 * 认证流程：
 * 1. 用户登录 → 返回JWT Token
 * 2. 前端保存Token，后续请求携带Authorization头
 * 3. 后端验证Token，提取用户信息
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 用户服务，负责用户查询和创建 */
    private final UserService userService;

    /** JWT工具类，负责Token的生成和解析 */
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     *
     * 请求：POST /api/auth/login
     * 请求体：{"username": "admin", "password": "123456"}
     * 响应：{"code": 200, "message": "登录成功", "data": {"token": "...", "userId": 1, ...}}
     *
     * @param request 登录请求（包含用户名和密码）
     * @return 登录响应（包含Token和用户信息），失败返回错误信息
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. 查询用户
        UserEntity user = userService.findByUsername(request.getUsername());
        if (user == null) {
            return ApiResponse.error(401, "用户名或密码错误");
        }

        // 2. 检查账号状态（1=启用，0=停用）
        if (user.getStatus() != 1) {
            return ApiResponse.error(403, "账号已被停用，请联系管理员");
        }

        // 3. 验证密码
        if (!userService.checkPassword(user, request.getPassword())) {
            return ApiResponse.error(401, "用户名或密码错误");
        }

        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 5. 构建响应
        LoginResponse response = new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getDepartmentId()
        );
        return ApiResponse.success("登录成功", response);
    }

    /**
     * 用户注册
     *
     * 请求：POST /api/auth/register
     * 请求体：{"username": "newuser", "password": "123456", "nickname": "新用户", "phone": "13800138000", "email": "user@example.com"}
     * 响应：{"code": 200, "message": "注册成功", "data": {"token": "...", "userId": 2, ...}}
     *
     * @param request 注册请求（包含用户名、密码、昵称等）
     * @return 注册响应（包含Token和用户信息），失败返回错误信息
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        // 检查用户名是否已存在
        if (userService.findByUsername(request.getUsername()) != null) {
            return ApiResponse.error(400, "用户名已存在");
        }

        // 注册默认为USER角色
        UserEntity user = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getNickname(),
                request.getPhone(),
                request.getEmail(),
                "USER"
        );

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 构建响应
        LoginResponse response = new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                null  // 注册时没有科室ID
        );
        return ApiResponse.success("注册成功", response);
    }

    /**
     * 获取当前登录用户信息
     *
     * 请求：GET /api/auth/me
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "data": {"userId": 1, "username": "admin", ...}}
     *
     * @param authHeader Authorization请求头（Bearer token）
     * @return 用户信息，认证失败返回401错误
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // 从Authorization头提取Token（去掉"Bearer "前缀）
            String token = authHeader.substring(7);

            // 从Token中解析用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 查询用户信息
            UserEntity user = userService.findById(userId);
            if (user == null) {
                return ApiResponse.error(404, "用户不存在");
            }

            // 构建响应（不返回新Token）
            LoginResponse response = new LoginResponse(
                    null, // 不返回新token
                    user.getId(),
                    user.getUsername(),
                    user.getNickname(),
                    user.getRole(),
                    user.getDepartmentId()
            );
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error(401, "认证失败");
        }
    }
}
