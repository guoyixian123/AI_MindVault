package org.example.major_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI_MindVault 应用程序启动类
 *
 * 项目简介：
 * AI_MindVault 是一个医疗健康智能咨询助手系统，基于AI Agent A.R.I.A（Advanced Responsive Intelligent Advisor）
 *
 * 核心功能：
 * 1. AI智能问诊 - 基于LangChain4j + Qwen大模型
 * 2. RAG知识库 - 疾病、药品、症状等医疗知识检索
 * 3. 聊天记忆 - Redis存储，支持多会话隔离
 * 4. 体检报告解读 - AI分析体检报告
 * 5. 症状自查 - AI辅助症状分析
 * 6. 药品查询 - 药品信息、药物联用检测
 * 7. 预约挂号 - 在线预约医生
 * 8. 健康档案 - 个人健康记录管理
 *
 * 技术栈：
 * - 后端：Spring Boot 3 + Java 21
 * - AI框架：LangChain4j
 * - AI模型：阿里云Qwen（通义千问）
 * - 数据库：MySQL + MyBatis-Plus
 * - 缓存：Redis（聊天记忆 + 向量存储）
 * - 前端：Vue 3 + Vite
 */
@SpringBootApplication
public class MajorAiApplication {

    /**
     * 应用程序入口
     * 启动Spring Boot应用，自动配置所有组件
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MajorAiApplication.class, args);
    }

}
