package org.example.major_ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.major_ai.entity.UserEntity;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
