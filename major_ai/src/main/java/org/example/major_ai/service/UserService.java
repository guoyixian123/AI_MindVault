package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.UserEntity;
import org.example.major_ai.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务
 * 负责用户的CRUD操作和密码验证
 *
 * 调用方：
 * - AuthController：登录、注册、获取当前用户
 * - AdminUserController：用户管理
 * - ConsultationService：查询医生信息
 */
@Service
@RequiredArgsConstructor
public class UserService {

    /** 用户Mapper，负责user表的CRUD */
    private final UserMapper userMapper;

    /** 密码编码器，用于密码加密和验证 */
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体，不存在返回null
     */
    public UserEntity findByUsername(String username) {
        // SQL: SELECT * FROM `user` WHERE username = ?
        return userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username)
        );
    }

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return 用户实体，不存在返回null
     */
    public UserEntity findById(Long id) {
        // SQL: SELECT * FROM `user` WHERE id = ?
        return userMapper.selectById(id);
    }

    /**
     * 创建用户
     *
     * @param username 用户名
     * @param password 密码（明文，会被BCrypt加密）
     * @param nickname 昵称（可选，默认为用户名）
     * @param phone 手机号（可选）
     * @param email 邮箱（可选）
     * @param role 角色（ROOT_ADMIN/DOCTOR/USER）
     * @return 创建成功的用户实体
     */
    public UserEntity createUser(String username, String password, String nickname,
                                  String phone, String email, String role) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));  // BCrypt加密密码
        user.setNickname(nickname != null ? nickname : username);  // 昵称默认为用户名
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(1);  // 默认启用
        long now = System.currentTimeMillis();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // SQL: INSERT INTO `user`(username, password_hash, nickname, phone, email, role, status, created_at, updated_at)
        //      VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)
        userMapper.insert(user);
        return user;
    }

    /**
     * 查询用户列表（支持按角色和状态筛选）
     *
     * @param role 角色（可选）
     * @param status 状态（可选，1=启用，0=停用）
     * @return 用户列表，按创建时间降序排列
     */
    public List<UserEntity> listUsers(String role, Integer status) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            wrapper.eq(UserEntity::getRole, role);
        }
        if (status != null) {
            wrapper.eq(UserEntity::getStatus, status);
        }
        wrapper.orderByDesc(UserEntity::getCreatedAt);

        // SQL: SELECT * FROM `user` WHERE role = ? AND status = ? ORDER BY created_at DESC
        return userMapper.selectList(wrapper);
    }

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 新状态（1=启用，0=停用）
     * @return 更新成功返回true
     */
    public boolean updateUserStatus(Long userId, Integer status) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setStatus(status);
        user.setUpdatedAt(System.currentTimeMillis());

        // SQL: UPDATE `user` SET status = ?, updated_at = ? WHERE id = ?
        return userMapper.updateById(user) > 0;
    }

    /**
     * 更新用户科室
     *
     * @param userId 用户ID
     * @param departmentId 科室ID
     * @return 更新成功返回true
     */
    public boolean updateUserDepartment(Long userId, Long departmentId) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setDepartmentId(departmentId);
        user.setUpdatedAt(System.currentTimeMillis());

        // SQL: UPDATE `user` SET department_id = ?, updated_at = ? WHERE id = ?
        return userMapper.updateById(user) > 0;
    }

    /**
     * 验证密码
     *
     * @param user 用户实体
     * @param rawPassword 明文密码
     * @return 密码匹配返回true
     */
    public boolean checkPassword(UserEntity user, String rawPassword) {
        // 使用BCrypt验证密码
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}
