package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.UserEntity;
import org.example.major_ai.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserEntity findByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username)
        );
    }

    public UserEntity findById(Long id) {
        return userMapper.selectById(id);
    }

    public UserEntity createUser(String username, String password, String nickname,
                                  String phone, String email, String role) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(1);
        long now = System.currentTimeMillis();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return user;
    }

    public List<UserEntity> listUsers(String role, Integer status) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            wrapper.eq(UserEntity::getRole, role);
        }
        if (status != null) {
            wrapper.eq(UserEntity::getStatus, status);
        }
        wrapper.orderByDesc(UserEntity::getCreatedAt);
        return userMapper.selectList(wrapper);
    }

    public boolean updateUserStatus(Long userId, Integer status) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setStatus(status);
        user.setUpdatedAt(System.currentTimeMillis());
        return userMapper.updateById(user) > 0;
    }

    public boolean updateUserDepartment(Long userId, Long departmentId) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setDepartmentId(departmentId);
        user.setUpdatedAt(System.currentTimeMillis());
        return userMapper.updateById(user) > 0;
    }

    public List<UserEntity> listDoctorsByDepartment(Long departmentId) {
        return userMapper.selectList(
                new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getRole, "DOCTOR")
                        .eq(UserEntity::getDepartmentId, departmentId)
                        .eq(UserEntity::getStatus, 1)
        );
    }

    public boolean checkPassword(UserEntity user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}
