# AI_MindVault - 法律智能助手

基于 LangChain4j + RAG 的个人法律智能助手，支持流式输出、会话管理和知识库检索。

## 技术栈

- Java 21
- Spring Boot 3.5.12
- LangChain4j 1.0.1-beta6
- MyBatis-Plus 3.5.7
- MySQL 8.0+
- Redis (Redis Stack)
- Vue 3

## 快速开始

### 1. 环境准备

- JDK 21+
- MySQL 8.0+
- Redis Stack (支持向量存储)
- 通义千问 API Key (或其他 OpenAI 兼容模型)

### 2. 配置文件

复制配置模板并填入真实配置：

```bash
cd major_ai/src/main/resources
cp application.yml.example application.yml
```

编辑 `application.yml`，填入：
- MySQL 连接信息
- Redis 连接信息
- API Key

### 3. 创建数据库

```sql
CREATE DATABASE ai_mindvault DEFAULT CHARACTER SET utf8mb4;
USE ai_mindvault;

-- 执行 schema.sql 中的建表语句
```

### 4. 启动应用

```bash
cd major_ai
mvn spring-boot:run
```

访问 http://localhost:8080

## 功能特性

- ✅ AI 法律咨询（流式输出）
- ✅ 会话管理（创建/切换/删除）
- ✅ 聊天记录持久化（MySQL）
- ✅ RAG 知识库检索（Redis 向量存储）
- ✅ 响应式前端界面

## 项目结构

```
major_ai/
├── src/main/java/org/example/major_ai/
│   ├── MajorAiApplication.java          # 启动类
│   ├── controller/                      # 控制器
│   ├── service/                         # 服务层
│   ├── mapper/                          # MyBatis Mapper
│   ├── entity/                          # 实体类
│   ├── config/                          # 配置类
│   └── aiservice/                       # AI 服务接口
└── src/main/resources/
    ├── application.yml.example          # 配置模板
    ├── schema.sql                       # 数据库表结构
    ├── system.txt                       # AI 系统提示词
    └── static/                          # 前端页面
```

## 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| QWEN_API_KEY | 通义千问 API Key | - |
| MYSQL_HOST | MySQL 主机 | localhost |
| MYSQL_PORT | MySQL 端口 | 3306 |
| MYSQL_DATABASE | 数据库名 | ai_mindvault |
| MYSQL_USERNAME | MySQL 用户名 | root |
| MYSQL_PASSWORD | MySQL 密码 | - |
| REDIS_HOST | Redis 主机 | localhost |
| REDIS_PORT | Redis 端口 | 6379 |
| REDIS_PASSWORD | Redis 密码 | - |

### 切换模型

修改 `application.yml` 中的 `base-url` 和 `model-name`：

```yaml
# DeepSeek
base-url: https://api.deepseek.com/v1
model-name: deepseek-chat

# OpenAI
base-url: https://api.openai.com/v1
model-name: gpt-4o

# 本地 Ollama
base-url: http://localhost:11434/v1
model-name: qwen2.5
```

## 许可证

MIT License
