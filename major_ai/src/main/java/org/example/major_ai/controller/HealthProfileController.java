package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.HealthProfileEntity;
import org.example.major_ai.security.UserDetailsImpl;
import org.example.major_ai.service.HealthProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health/profile")
@RequiredArgsConstructor
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    @GetMapping
    public ApiResponse<HealthProfileEntity> getProfile(@AuthenticationPrincipal UserDetailsImpl user) {
        return getProfileByUserId(user.getId());
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<HealthProfileEntity> getProfileByUserId(@PathVariable Long userId) {
        HealthProfileEntity profile = healthProfileService.getByUserId(userId);
        if (profile == null) {
            return ApiResponse.success("暂无健康档案", null);
        }
        return ApiResponse.success(profile);
    }

    @PostMapping
    public ApiResponse<HealthProfileEntity> saveProfile(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody HealthProfileEntity profile) {
        profile.setUserId(user.getId());
        // 自动计算BMI
        if (profile.getHeight() != null && profile.getWeight() != null) {
            double heightM = profile.getHeight().doubleValue() / 100.0;
            double bmi = profile.getWeight().doubleValue() / (heightM * heightM);
            profile.setBmi(java.math.BigDecimal.valueOf(Math.round(bmi * 10) / 10.0));
        }
        HealthProfileEntity saved = healthProfileService.createOrUpdate(profile);
        return ApiResponse.success("健康档案保存成功", saved);
    }
}
