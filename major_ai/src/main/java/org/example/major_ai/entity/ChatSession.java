package org.example.major_ai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天会话DTO
 * 用于API响应的数据传输对象
 *
 * 与ChatSessionEntity的区别：
 * - ChatSessionEntity：数据库实体，包含MyBatis-Plus注解
 * - ChatSession：DTO对象，用于API响应，不包含数据库注解
 *
 * 响应示例：
 * {
 *   "id": "550e8400e29b41d4a716446655440000",
 *   "userId": null,
 *   "title": "我头疼怎么办",
 *   "createdAt": 1625097600000,
 *   "updatedAt": 1625097600000
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    /** 会话ID */
    private String id;

    /** 用户ID（暂未使用） */
    private String userId;

    /** 会话标题 */
    private String title;

    /** 创建时间（时间戳，毫秒） */
    private long createdAt;

    /** 更新时间（时间戳，毫秒） */
    private long updatedAt;
}
