package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.HealthRecordEntity;
import org.example.major_ai.mapper.HealthRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final HealthRecordMapper healthRecordMapper;

    public HealthRecordEntity save(HealthRecordEntity record) {
        record.setCreatedAt(System.currentTimeMillis());
        healthRecordMapper.insert(record);
        return record;
    }

    public List<HealthRecordEntity> listByUserId(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return healthRecordMapper.selectList(
                new LambdaQueryWrapper<HealthRecordEntity>()
                        .eq(HealthRecordEntity::getUserId, userId)
                        .ge(HealthRecordEntity::getRecordDate, startDate)
                        .orderByAsc(HealthRecordEntity::getRecordDate)
        );
    }

    public Map<String, Object> getTrendData(Long userId, String indicator, int days) {
        List<HealthRecordEntity> records = listByUserId(userId, days);

        Map<String, Object> result = new HashMap<>();
        result.put("indicator", indicator);
        result.put("days", days);

        List<Map<String, Object>> dataPoints = records.stream().map(r -> {
            Map<String, Object> point = new HashMap<>();
            point.put("date", r.getRecordDate().toString());
            switch (indicator) {
                case "blood_pressure_sys" -> point.put("value", r.getBloodPressureSys());
                case "blood_pressure_dia" -> point.put("value", r.getBloodPressureDia());
                case "blood_sugar" -> point.put("value", r.getBloodSugar());
                case "steps" -> point.put("value", r.getSteps());
                case "sleep_hours" -> point.put("value", r.getSleepHours());
                case "body_temperature" -> point.put("value", r.getBodyTemperature());
                default -> point.put("value", null);
            }
            return point;
        }).toList();

        result.put("data", dataPoints);

        // 异常指标检测
        List<String> warnings = records.stream().filter(r -> {
            if ("blood_pressure_sys".equals(indicator) && r.getBloodPressureSys() != null) {
                return r.getBloodPressureSys() > 140 || r.getBloodPressureSys() < 90;
            }
            if ("blood_pressure_dia".equals(indicator) && r.getBloodPressureDia() != null) {
                return r.getBloodPressureDia() > 90 || r.getBloodPressureDia() < 60;
            }
            if ("blood_sugar".equals(indicator) && r.getBloodSugar() != null) {
                return r.getBloodSugar().doubleValue() > 7.0 || r.getBloodSugar().doubleValue() < 3.9;
            }
            if ("body_temperature".equals(indicator) && r.getBodyTemperature() != null) {
                return r.getBodyTemperature().doubleValue() > 37.3 || r.getBodyTemperature().doubleValue() < 36.0;
            }
            return false;
        }).map(r -> r.getRecordDate().toString() + " 存在异常值").toList();

        result.put("warnings", warnings);
        return result;
    }
}
