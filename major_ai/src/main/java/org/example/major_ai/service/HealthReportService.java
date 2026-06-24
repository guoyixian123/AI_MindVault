package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.HealthReportEntity;
import org.example.major_ai.mapper.HealthReportMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthReportService {

    private final HealthReportMapper healthReportMapper;

    public HealthReportEntity saveReport(Long userId, String reportType, String reportContent) {
        HealthReportEntity report = new HealthReportEntity();
        report.setUserId(userId);
        report.setReportType(reportType);
        report.setReportContent(reportContent);
        report.setCreatedAt(System.currentTimeMillis());
        healthReportMapper.insert(report);
        return report;
    }

    public List<HealthReportEntity> listByUserId(Long userId) {
        return healthReportMapper.selectList(
                new LambdaQueryWrapper<HealthReportEntity>()
                        .eq(HealthReportEntity::getUserId, userId)
                        .orderByDesc(HealthReportEntity::getCreatedAt)
        );
    }

    public HealthReportEntity findById(Long id) {
        return healthReportMapper.selectById(id);
    }

    public boolean updateAnalysis(Long id, String aiAnalysis) {
        HealthReportEntity report = new HealthReportEntity();
        report.setId(id);
        report.setAiAnalysis(aiAnalysis);
        return healthReportMapper.updateById(report) > 0;
    }
}
