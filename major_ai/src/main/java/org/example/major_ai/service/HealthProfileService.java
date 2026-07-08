package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.HealthProfileEntity;
import org.example.major_ai.mapper.HealthProfileMapper;
import org.springframework.stereotype.Service;

/**
 * 健康档案服务
 * 负责用户健康档案的查询和更新
 *
 * 特点：
 * - 每个用户只有一个健康档案
 * - 支持创建或更新（upsert语义）
 *
 * 调用方：
 * - HealthProfileController：健康档案接口
 */
@Service
@RequiredArgsConstructor
public class HealthProfileService {

    /** 健康档案Mapper，负责health_profile表的CRUD */
    private final HealthProfileMapper healthProfileMapper;

    /**
     * 根据用户ID查询健康档案
     *
     * @param userId 用户ID
     * @return 健康档案，不存在返回null
     */
    public HealthProfileEntity getByUserId(Long userId) {
        // SQL: SELECT * FROM health_profile WHERE user_id = ?
        return healthProfileMapper.selectOne(
                new LambdaQueryWrapper<HealthProfileEntity>()
                        .eq(HealthProfileEntity::getUserId, userId)
        );
    }

    /**
     * 创建或更新健康档案
     * 如果已存在则更新，不存在则创建
     *
     * @param profile 健康档案实体
     * @return 更新后的健康档案
     */
    public HealthProfileEntity createOrUpdate(HealthProfileEntity profile) {
        // 查询是否已存在
        HealthProfileEntity existing = getByUserId(profile.getUserId());
        long now = System.currentTimeMillis();

        if (existing != null) {
            // 已存在，更新
            profile.setId(existing.getId());
            profile.setUpdatedAt(now);

            // SQL: UPDATE health_profile SET blood_type = ?, height = ?, weight = ?, ... WHERE id = ?
            healthProfileMapper.updateById(profile);
        } else {
            // 不存在，创建
            profile.setCreatedAt(now);
            profile.setUpdatedAt(now);

            // SQL: INSERT INTO health_profile(user_id, blood_type, height, weight, ...)
            //      VALUES(?, ?, ?, ?, ...)
            healthProfileMapper.insert(profile);
        }

        // 返回更新后的数据
        return getByUserId(profile.getUserId());
    }
}
