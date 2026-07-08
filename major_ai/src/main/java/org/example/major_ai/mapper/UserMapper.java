package org.example.major_ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.major_ai.entity.UserEntity;

/**
 * 用户Mapper
 * 负责user表的数据库操作
 *
 * 继承BaseMapper<UserEntity>，自动获得以下方法：
 * - insert(entity): 插入记录
 * - selectById(id): 根据ID查询
 * - selectOne(wrapper): 条件查询单条
 * - selectList(wrapper): 条件查询列表
 * - updateById(entity): 根据ID更新
 * - deleteById(id): 根据ID删除
 * - ...
 *
 * 使用示例：
 * userMapper.insert(entity);
 * userMapper.selectById(userId);
 */
@Mapper  // MyBatis注解：标识为Mapper接口
public interface UserMapper extends BaseMapper<UserEntity> {
    // 无需编写SQL，MyBatis-Plus自动生成
}
