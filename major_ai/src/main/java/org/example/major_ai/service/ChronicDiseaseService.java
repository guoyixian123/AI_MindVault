package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ChronicDiseaseEntity;
import org.example.major_ai.entity.ChronicRecordEntity;
import org.example.major_ai.mapper.ChronicDiseaseMapper;
import org.example.major_ai.mapper.ChronicRecordMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChronicDiseaseService {

    private final ChronicDiseaseMapper chronicDiseaseMapper;
    private final ChronicRecordMapper chronicRecordMapper;

    public ChronicDiseaseEntity create(ChronicDiseaseEntity disease) {
        long now = System.currentTimeMillis();
        disease.setCreatedAt(now);
        disease.setUpdatedAt(now);
        chronicDiseaseMapper.insert(disease);
        return disease;
    }

    public List<ChronicDiseaseEntity> listByUserId(Long userId) {
        return chronicDiseaseMapper.selectList(
                new LambdaQueryWrapper<ChronicDiseaseEntity>()
                        .eq(ChronicDiseaseEntity::getUserId, userId)
                        .orderByDesc(ChronicDiseaseEntity::getCreatedAt)
        );
    }

    public ChronicDiseaseEntity findById(Long id) {
        return chronicDiseaseMapper.selectById(id);
    }

    public boolean update(ChronicDiseaseEntity disease) {
        disease.setUpdatedAt(System.currentTimeMillis());
        return chronicDiseaseMapper.updateById(disease) > 0;
    }

    public boolean delete(Long id) {
        // 先删除关联的指标记录
        chronicRecordMapper.delete(
                new LambdaQueryWrapper<ChronicRecordEntity>()
                        .eq(ChronicRecordEntity::getChronicDiseaseId, id)
        );
        return chronicDiseaseMapper.deleteById(id) > 0;
    }

    // 慢病指标记录
    public ChronicRecordEntity addRecord(ChronicRecordEntity record) {
        record.setCreatedAt(System.currentTimeMillis());
        chronicRecordMapper.insert(record);
        return record;
    }

    public List<ChronicRecordEntity> listRecords(Long chronicDiseaseId) {
        return chronicRecordMapper.selectList(
                new LambdaQueryWrapper<ChronicRecordEntity>()
                        .eq(ChronicRecordEntity::getChronicDiseaseId, chronicDiseaseId)
                        .orderByDesc(ChronicRecordEntity::getRecordDate)
        );
    }
}
