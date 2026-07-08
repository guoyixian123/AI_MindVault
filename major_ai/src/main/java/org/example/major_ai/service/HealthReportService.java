package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.HealthReportEntity;
import org.example.major_ai.mapper.HealthReportMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 健康报告服务
 * 负责体检报告的存储和AI分析结果管理
 *
 * 功能：
 * 1. 保存体检报告
 * 2. 查询用户报告列表
 * 3. 更新AI分析结果（由ChatWebSocketHandler调用）
 *
 * 调用方：
 * - HealthReportController：报告接口
 * - ChatWebSocketHandler：AI分析完成后回写结果
 */
@Service
@RequiredArgsConstructor
public class HealthReportService {

    /** 健康报告Mapper，负责health_report表的CRUD */
    private final HealthReportMapper healthReportMapper;

    /**
     * 保存体检报告
     *
     * @param userId 用户ID
     * @param reportType 报告类型（如：综合体检报告、血常规、尿常规等）
     * @param reportContent 报告内容
     * @return 保存成功的报告
     */
    public HealthReportEntity saveReport(Long userId, String reportType, String reportContent) {
        HealthReportEntity report = new HealthReportEntity();
        report.setUserId(userId);
        report.setReportType(reportType);
        report.setReportContent(reportContent);
        report.setCreatedAt(System.currentTimeMillis());

        // SQL: INSERT INTO health_report(user_id, report_type, report_content, created_at)
        //      VALUES(?, ?, ?, ?)
        healthReportMapper.insert(report);
        return report;
    }

    /**
     * 查询用户的报告列表
     *
     * @param userId 用户ID
     * @return 报告列表，按创建时间降序排列
     */
    public List<HealthReportEntity> listByUserId(Long userId) {
        // SQL: SELECT * FROM health_report WHERE user_id = ? ORDER BY created_at DESC
        return healthReportMapper.selectList(
                new LambdaQueryWrapper<HealthReportEntity>()
                        .eq(HealthReportEntity::getUserId, userId)
                        .orderByDesc(HealthReportEntity::getCreatedAt)
        );
    }

    /**
     * 根据ID查询报告
     *
     * @param id 报告ID
     * @return 报告实体，不存在返回null
     */
    public HealthReportEntity findById(Long id) {
        // SQL: SELECT * FROM health_report WHERE id = ?
        return healthReportMapper.selectById(id);
    }

    /**
     * 更新AI分析结果
     * 由ChatWebSocketHandler在AI分析完成后调用
     *
     * @param id 报告ID
     * @param aiAnalysis AI分析结果
     * @return 更新成功返回true
     */
    public boolean updateAnalysis(Long id, String aiAnalysis) {
        HealthReportEntity report = new HealthReportEntity();
        report.setId(id);
        report.setAiAnalysis(aiAnalysis);

        // SQL: UPDATE health_report SET ai_analysis = ? WHERE id = ?
        return healthReportMapper.updateById(report) > 0;
    }
}
