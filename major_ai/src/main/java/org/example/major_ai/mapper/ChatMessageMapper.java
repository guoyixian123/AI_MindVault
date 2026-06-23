package org.example.major_ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.major_ai.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {
}
