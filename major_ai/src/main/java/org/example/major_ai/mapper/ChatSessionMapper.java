package org.example.major_ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.major_ai.entity.ChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {
}
