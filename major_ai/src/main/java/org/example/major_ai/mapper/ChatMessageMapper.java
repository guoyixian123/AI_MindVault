package org.example.major_ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.major_ai.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息Mapper
 * 负责chat_message表的数据库操作
 *
 * 继承BaseMapper<ChatMessageEntity>，自动获得以下方法：
 * - insert(entity): 插入记录
 * - selectById(id): 根据ID查询
 * - selectList(wrapper): 条件查询
 * - updateById(entity): 根据ID更新
 * - deleteById(id): 根据ID删除
 * - delete(wrapper): 条件删除
 * - ...
 *
 * 使用示例：
 * chatMessageMapper.insert(entity);
 * chatMessageMapper.selectList(wrapper);
 */
@Mapper  // MyBatis注解：标识为Mapper接口
public interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {
    // 无需编写SQL，MyBatis-Plus自动生成
}
