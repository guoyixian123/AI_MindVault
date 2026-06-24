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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UserEntity user = userService.findByUsername(request.getUsername());
        if (user == null) {
            return ApiResponse.error(401, "用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            return ApiResponse.error(403, "账号已被停用，请联系管理员");
        }
        if (!userService.checkPassword(user, request.getPassword())) {
            return ApiResponse.error(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
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

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginResponse response = new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                null
        );
        return ApiResponse.success("注册成功", response);
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            UserEntity user = userService.findById(userId);
            if (user == null) {
                return ApiResponse.error(404, "用户不存在");
            }
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
