package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.HealthProfileEntity;
import org.example.major_ai.security.UserDetailsImpl;
import org.example.major_ai.service.HealthProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 健康档案控制器
 * 提供健康档案的查询和保存接口
 *
 * API前缀：/api/health/profile
 *
 * 功能：
 * - 查询当前用户的健康档案
 * - 查询指定用户的健康档案
 * - 保存/更新健康档案（自动计算BMI）
 */
@RestController
@RequestMapping("/api/health/profile")
@RequiredArgsConstructor
public class HealthProfileController {

    /** 健康档案服务 */
    private final HealthProfileService healthProfileService;

    /**
     * 查询当前用户的健康档案
     *
     * 请求：GET /api/health/profile
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "data": {...}}
     *
     * @param user 当前登录用户
     * @return 健康档案
     */
    @GetMapping
    public ApiResponse<HealthProfileEntity> getProfile(@AuthenticationPrincipal UserDetailsImpl user) {
        return getProfileByUserId(user.getId());
    }

    /**
     * 查询指定用户的健康档案
     *
     * 请求：GET /api/health/profile/user/{userId}
     * 响应：{"code": 200, "data": {...}}
     *
     * @param userId 用户ID
     * @return 健康档案，不存在返回null
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<HealthProfileEntity> getProfileByUserId(@PathVariable Long userId) {
        HealthProfileEntity profile = healthProfileService.getByUserId(userId);
        if (profile == null) {
            return ApiResponse.success("暂无健康档案", null);
        }
        return ApiResponse.success(profile);
    }

    /**
     * 保存/更新健康档案
     *
     * 请求：POST /api/health/profile
     * 请求头：Authorization: Bearer {token}
     * 请求体：{"bloodType": "A", "height": 175, "weight": 70, ...}
     * 响应：{"code": 200, "message": "健康档案保存成功", "data": {...}}
     *
     * 特性：自动根据身高体重计算BMI
     *
     * @param user 当前登录用户
     * @param profile 健康档案实体
     * @return 保存成功的健康档案
     */
    @PostMapping
    public ApiResponse<HealthProfileEntity> saveProfile(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody HealthProfileEntity profile) {
        // 设置用户ID
        profile.setUserId(user.getId());

        // 自动计算BMI
        if (profile.getHeight() != null && profile.getWeight() != null) {
            double heightM = profile.getHeight().doubleValue() / 100.0;  // 厘米转米
            double bmi = profile.getWeight().doubleValue() / (heightM * heightM);  // BMI = 体重(kg) / 身高(m)²
            profile.setBmi(java.math.BigDecimal.valueOf(Math.round(bmi * 10) / 10.0));  // 保留一位小数
        }

        HealthProfileEntity saved = healthProfileService.createOrUpdate(profile);
        return ApiResponse.success("健康档案保存成功", saved);
    }
}
