package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ChronicDiseaseEntity;
import org.example.major_ai.entity.ChronicRecordEntity;
import org.example.major_ai.mapper.ChronicDiseaseMapper;
import org.example.major_ai.mapper.ChronicRecordMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 慢病管理服务
 * 负责慢性疾病和指标记录的管理
 *
 * 数据结构：
 * - ChronicDisease: 慢病基本信息（如：高血压、糖尿病）
 * - ChronicRecord: 慢病指标记录（如：血压值、血糖值）
 *
 * 关系：
 * - 一个用户可以有多个慢病
 * - 一个慢病可以有多条指标记录
 *
 * 调用方：
 * - ChronicDiseaseController：慢病管理接口
 */
@Service
@RequiredArgsConstructor
public class ChronicDiseaseService {

    /** 慢病Mapper，负责chronic_disease表的CRUD */
    private final ChronicDiseaseMapper chronicDiseaseMapper;

    /** 慢病记录Mapper，负责chronic_record表的CRUD */
    private final ChronicRecordMapper chronicRecordMapper;

    /**
     * 创建慢病
     *
     * @param disease 慢病实体
     * @return 创建成功的慢病
     */
    public ChronicDiseaseEntity create(ChronicDiseaseEntity disease) {
        long now = System.currentTimeMillis();
        disease.setCreatedAt(now);
        disease.setUpdatedAt(now);

        // SQL: INSERT INTO chronic_disease(user_id, disease_name, diagnosis_date, ...)
        //      VALUES(?, ?, ?, ...)
        chronicDiseaseMapper.insert(disease);
        return disease;
    }

    /**
     * 查询用户的慢病列表
     *
     * @param userId 用户ID
     * @return 慢病列表，按创建时间降序排列
     */
    public List<ChronicDiseaseEntity> listByUserId(Long userId) {
        // SQL: SELECT * FROM chronic_disease WHERE user_id = ? ORDER BY created_at DESC
        return chronicDiseaseMapper.selectList(
                new LambdaQueryWrapper<ChronicDiseaseEntity>()
                        .eq(ChronicDiseaseEntity::getUserId, userId)
                        .orderByDesc(ChronicDiseaseEntity::getCreatedAt)
        );
    }

    /**
     * 根据ID查询慢病
     *
     * @param id 慢病ID
     * @return 慢病实体，不存在返回null
     */
    public ChronicDiseaseEntity findById(Long id) {
        // SQL: SELECT * FROM chronic_disease WHERE id = ?
        return chronicDiseaseMapper.selectById(id);
    }

    /**
     * 更新慢病信息
     *
     * @param disease 慢病实体（包含ID和要更新的字段）
     * @return 更新成功返回true
     */
    public boolean update(ChronicDiseaseEntity disease) {
        disease.setUpdatedAt(System.currentTimeMillis());

        // SQL: UPDATE chronic_disease SET disease_name = ?, ... WHERE id = ?
        return chronicDiseaseMapper.updateById(disease) > 0;
    }

    /**
     * 删除慢病及其关联的指标记录
     *
     * @param id 慢病ID
     * @return 删除成功返回true
     */
    public boolean delete(Long id) {
        // 先删除关联的指标记录
        // SQL: DELETE FROM chronic_record WHERE chronic_disease_id = ?
        chronicRecordMapper.delete(
                new LambdaQueryWrapper<ChronicRecordEntity>()
                        .eq(ChronicRecordEntity::getChronicDiseaseId, id)
        );

        // 再删除慢病
        // SQL: DELETE FROM chronic_disease WHERE id = ?
        return chronicDiseaseMapper.deleteById(id) > 0;
    }

    /**
     * 添加慢病指标记录
     *
     * @param record 指标记录实体
     * @return 创建成功的记录
     */
    public ChronicRecordEntity addRecord(ChronicRecordEntity record) {
        record.setCreatedAt(System.currentTimeMillis());

        // SQL: INSERT INTO chronic_record(chronic_disease_id, record_date, indicator_value, ...)
        //      VALUES(?, ?, ?, ...)
        chronicRecordMapper.insert(record);
        return record;
    }

    /**
     * 查询慢病的指标记录列表
     *
     * @param chronicDiseaseId 慢病ID
     * @return 指标记录列表，按记录日期降序排列
     */
    public List<ChronicRecordEntity> listRecords(Long chronicDiseaseId) {
        // SQL: SELECT * FROM chronic_record WHERE chronic_disease_id = ? ORDER BY record_date DESC
        return chronicRecordMapper.selectList(
                new LambdaQueryWrapper<ChronicRecordEntity>()
                        .eq(ChronicRecordEntity::getChronicDiseaseId, chronicDiseaseId)
                        .orderByDesc(ChronicRecordEntity::getRecordDate)
        );
    }
}
