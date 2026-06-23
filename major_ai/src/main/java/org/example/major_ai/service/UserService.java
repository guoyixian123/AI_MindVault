package org.example.major_ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    private static final String USER_KEY_PREFIX = "user:";
    private static final String USERNAME_INDEX_KEY = "user:username:";

    public User register(String username, String password) {
        // 检查用户名是否已存在
        String existingUserId = redisTemplate.opsForValue().get(USERNAME_INDEX_KEY + username);
        if (existingUserId != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        String userId = UUID.randomUUID().toString().replace("-", "");
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedAt(System.currentTimeMillis());

        // 存储用户信息
        try {
            String userJson = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(USER_KEY_PREFIX + userId, userJson, 30, TimeUnit.DAYS);
            // 存储用户名索引
            redisTemplate.opsForValue().set(USERNAME_INDEX_KEY + username, userId, 30, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("用户注册失败", e);
        }

        // 返回时清除密码
        user.setPassword(null);
        return user;
    }

    public User login(String username, String password) {
        // 根据用户名查找用户ID
        String userId = redisTemplate.opsForValue().get(USERNAME_INDEX_KEY + username);
        if (userId == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 获取用户信息
        String userJson = redisTemplate.opsForValue().get(USER_KEY_PREFIX + userId);
        if (userJson == null) {
            throw new RuntimeException("用户不存在");
        }

        try {
            User user = objectMapper.readValue(userJson, User.class);
            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("用户名或密码错误");
            }
            // 返回时清除密码
            user.setPassword(null);
            return user;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("登录失败", e);
        }
    }

    public User getUserById(String userId) {
        String userJson = redisTemplate.opsForValue().get(USER_KEY_PREFIX + userId);
        if (userJson == null) {
            return null;
        }
        try {
            User user = objectMapper.readValue(userJson, User.class);
            user.setPassword(null);
            return user;
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
