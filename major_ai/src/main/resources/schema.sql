-- ==================== 原有表 ====================

-- 聊天会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) DEFAULT '新对话',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    INDEX idx_session_id (session_id),
    FOREIGN KEY (session_id) REFERENCES chat_session(id) ON DELETE CASCADE
);

-- ==================== 新增表 ====================

-- 科室表
CREATE TABLE IF NOT EXISTS department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    icon VARCHAR(100),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '1-启用 0-停用',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    INDEX idx_sort_order (sort_order)
);

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'ROOT_ADMIN/DOCTOR/USER',
    department_id BIGINT,
    status TINYINT DEFAULT 1 COMMENT '1-启用 0-停用',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_department (department_id),
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL
);

-- 个人健康档案
CREATE TABLE IF NOT EXISTS health_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    height DECIMAL(5,1) COMMENT '身高cm',
    weight DECIMAL(5,1) COMMENT '体重kg',
    bmi DECIMAL(4,1),
    blood_pressure_sys INT COMMENT '收缩压',
    blood_pressure_dia INT COMMENT '舒张压',
    blood_sugar DECIMAL(4,1) COMMENT '空腹血糖mmol/L',
    blood_lipid VARCHAR(100) COMMENT '血脂信息',
    allergies TEXT COMMENT '过敏史',
    family_history TEXT COMMENT '家族病史',
    surgery_history TEXT COMMENT '手术史',
    major_diseases TEXT COMMENT '重大疾病史',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
);

-- 日常健康数据记录
CREATE TABLE IF NOT EXISTS health_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    blood_pressure_sys INT,
    blood_pressure_dia INT,
    blood_sugar DECIMAL(4,1),
    steps INT,
    sleep_hours DECIMAL(3,1),
    body_temperature DECIMAL(3,1),
    menstrual_note VARCHAR(200) COMMENT '生理期记录',
    created_at BIGINT NOT NULL,
    INDEX idx_user_date (user_id, record_date),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
);

-- 体检报告
CREATE TABLE IF NOT EXISTS health_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    report_type VARCHAR(50) NOT NULL COMMENT '体检报告/血常规/彩超等',
    report_content TEXT COMMENT '报告文字内容',
    upload_file_path VARCHAR(500),
    ai_analysis TEXT COMMENT 'AI解读结果',
    created_at BIGINT NOT NULL,
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
);

-- 问诊帖子
CREATE TABLE IF NOT EXISTS consultation_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    images JSON COMMENT '图片URL数组',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/REPLIED/CLOSED',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    INDEX idx_department (department_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE
);

-- 医生回复
CREATE TABLE IF NOT EXISTS doctor_reply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    INDEX idx_post (post_id),
    FOREIGN KEY (post_id) REFERENCES consultation_post(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES `user`(id) ON DELETE CASCADE
);

-- 预约挂号
CREATE TABLE IF NOT EXISTS appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    doctor_id BIGINT,
    appointment_time DATETIME NOT NULL,
    complaint VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/CANCELLED/COMPLETED',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    INDEX idx_user (user_id),
    INDEX idx_department (department_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_time (appointment_time),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES `user`(id) ON DELETE SET NULL
);

-- ==================== 初始化科室数据 ====================
INSERT IGNORE INTO department (name, description, icon, sort_order, status, created_at, updated_at) VALUES
('消化内科', '消化系统疾病诊治', 'stomach', 1, 1, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('心血管科', '心血管系统疾病诊治', 'heart', 2, 1, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('神经内科', '神经系统疾病诊治', 'brain', 3, 1, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('皮肤科', '皮肤疾病诊治', 'skin', 4, 1, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('骨科', '骨骼关节疾病诊治', 'bone', 5, 1, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('呼吸内科', '呼吸系统疾病诊治', 'lung', 6, 1, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('内分泌科', '内分泌代谢疾病诊治', 'hormone', 7, 1, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('儿科', '儿童疾病诊治', 'child', 8, 1, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('妇产科', '妇产科疾病诊治', 'gynecology', 9, 1, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('泌尿外科', '泌尿系统疾病诊治', 'urology', 10, 1, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000);

-- ==================== 初始化根管理员账号 ====================
-- 密码: admin123 (BCrypt加密)
INSERT IGNORE INTO `user` (username, password_hash, nickname, role, status, created_at, updated_at) VALUES
('admin', '$2b$10$2WJ2CA9Y9Oacy9iQmVKi6.QaY4eGBGsoUTCU1T.DeCpYKIYsH476y', '系统管理员', 'ROOT_ADMIN', 1, UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000);
