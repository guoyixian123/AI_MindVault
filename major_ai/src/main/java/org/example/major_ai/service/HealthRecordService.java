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

/**
 * 健康记录服务
 * 负责健康数据的记录和趋势分析
 *
 * 支持的健康指标：
 * - blood_pressure_sys: 收缩压（高压）
 * - blood_pressure_dia: 舒张压（低压）
 * - blood_sugar: 血糖
 * - steps: 步数
 * - sleep_hours: 睡眠时长
 * - body_temperature: 体温
 *
 * 异常检测阈值：
 * - 收缩压：90-140 mmHg
 * - 舒张压：60-90 mmHg
 * - 血糖：3.9-7.0 mmol/L
 * - 体温：36.0-37.3 ℃
 *
 * 调用方：
 * - HealthRecordController：健康记录接口
 */
@Service
@RequiredArgsConstructor
public class HealthRecordService {

    /** 健康记录Mapper，负责health_record表的CRUD */
    private final HealthRecordMapper healthRecordMapper;

    /**
     * 保存健康记录
     *
     * @param record 健康记录实体
     * @return 保存成功的记录
     */
    public HealthRecordEntity save(HealthRecordEntity record) {
        record.setCreatedAt(System.currentTimeMillis());

        // SQL: INSERT INTO health_record(user_id, record_date, blood_pressure_sys, blood_pressure_dia, ...)
        //      VALUES(?, ?, ?, ?, ...)
        healthRecordMapper.insert(record);
        return record;
    }

    /**
     * 查询用户最近N天的健康记录
     *
     * @param userId 用户ID
     * @param days 天数
     * @return 健康记录列表，按日期升序排列
     */
    public List<HealthRecordEntity> listByUserId(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);

        // SQL: SELECT * FROM health_record
        //      WHERE user_id = ? AND record_date >= ?
        //      ORDER BY record_date ASC
        return healthRecordMapper.selectList(
                new LambdaQueryWrapper<HealthRecordEntity>()
                        .eq(HealthRecordEntity::getUserId, userId)
                        .ge(HealthRecordEntity::getRecordDate, startDate)
                        .orderByAsc(HealthRecordEntity::getRecordDate)
        );
    }

    /**
     * 获取健康指标趋势数据
     *
     * @param userId 用户ID
     * @param indicator 指标名称
     * @param days 天数
     * @return 趋势数据（包含数据点和异常警告）
     */
    public Map<String, Object> getTrendData(Long userId, String indicator, int days) {
        List<HealthRecordEntity> records = listByUserId(userId, days);

        Map<String, Object> result = new HashMap<>();
        result.put("indicator", indicator);
        result.put("days", days);

        // 构建数据点
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
