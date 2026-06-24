package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.HealthProfileEntity;
import org.example.major_ai.mapper.HealthProfileMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthProfileService {

    private final HealthProfileMapper healthProfileMapper;

    public HealthProfileEntity getByUserId(Long userId) {
        return healthProfileMapper.selectOne(
                new LambdaQueryWrapper<HealthProfileEntity>()
                        .eq(HealthProfileEntity::getUserId, userId)
        );
    }

    public HealthProfileEntity createOrUpdate(HealthProfileEntity profile) {
        HealthProfileEntity existing = getByUserId(profile.getUserId());
        long now = System.currentTimeMillis();

        if (existing != null) {
            profile.setId(existing.getId());
            profile.setUpdatedAt(now);
            healthProfileMapper.updateById(profile);
        } else {
            profile.setCreatedAt(now);
            profile.setUpdatedAt(now);
            healthProfileMapper.insert(profile);
        }
        return getByUserId(profile.getUserId());
    }
}
