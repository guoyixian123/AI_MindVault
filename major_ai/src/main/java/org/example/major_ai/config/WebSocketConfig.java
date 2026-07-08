package org.example.major_ai.config;

import org.example.major_ai.handler.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 注册WebSocket处理器，配置端点和跨域策略
 *
 * 端点：/ws/chat
 * 用途：AI对话的实时流式返回
 */
@Configuration
@EnableWebSocket  // 启用WebSocket支持
public class WebSocketConfig implements WebSocketConfigurer {

    /** WebSocket消息处理器，负责处理AI对话 */
    private final ChatWebSocketHandler chatWebSocketHandler;

    /**
     * 构造函数注入ChatWebSocketHandler
     *
     * @param chatWebSocketHandler WebSocket消息处理器
     */
    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    /**
     * 注册WebSocket处理器
     * 配置端点路径和跨域策略
     *
     * @param registry WebSocket处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")  // 注册处理器，端点为/ws/chat
                .setAllowedOrigins("*");  // 允许所有来源跨域访问（开发环境）
        // 生产环境应该限制为具体域名：setAllowedOrigins("https://yourdomain.com")
    }
}
