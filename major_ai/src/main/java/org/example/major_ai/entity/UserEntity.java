package org.example.major_ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户实体类
 * 对应数据库表：user
 *
 * 表结构：
 * CREATE TABLE `user` (
 *     id BIGINT AUTO_INCREMENT PRIMARY KEY,
 *     username VARCHAR(50) NOT NULL UNIQUE,
 *     password_hash VARCHAR(255) NOT NULL,
 *     nickname VARCHAR(50),
 *     phone VARCHAR(20),
 *     email VARCHAR(100),
 *     avatar VARCHAR(255),
 *     role VARCHAR(20) NOT NULL DEFAULT 'USER',
 *     department_id BIGINT,
 *     status INT NOT NULL DEFAULT 1,
 *     created_at BIGINT NOT NULL,
 *     updated_at BIGINT NOT NULL
 * );
 *
 * 角色说明：
 * - ROOT_ADMIN: 超级管理员，拥有所有权限
 * - DOCTOR: 医生，可以回复问诊帖
 * - USER: 普通用户，可以发帖、预约等
 */
@Data
@TableName("`user`")  // MyBatis-Plus注解：映射到user表（加反引号因为user是MySQL保留字）
public class UserEntity {

    /** 主键ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（唯一） */
    private String username;

    /** 密码哈希值（BCrypt加密） */
    @TableField("password_hash")
    private String passwordHash;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 角色：ROOT_ADMIN（管理员）、DOCTOR（医生）、USER（用户） */
    private String role;

    /** 科室ID（仅医生角色有值） */
    @TableField("department_id")
    private Long departmentId;

    /** 状态：1=启用，0=停用 */
    private Integer status;

    /** 创建时间（时间戳，毫秒） */
    @TableField("created_at")
    private Long createdAt;

    /** 更新时间（时间戳，毫秒） */
    @TableField("updated_at")
    private Long updatedAt;
}
