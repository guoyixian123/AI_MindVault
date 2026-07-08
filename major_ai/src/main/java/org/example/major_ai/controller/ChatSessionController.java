package org.example.major_ai.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.ChatMessageEntity;
import org.example.major_ai.entity.ChatSession;
import org.example.major_ai.service.ChatMessageService;
import org.example.major_ai.service.ChatSessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天会话控制器
 * 提供会话的CRUD接口（创建、查询、更新、删除）
 *
 * API前缀：/api/sessions
 *
 * 注意：AI对话通过WebSocket（/ws/chat）进行，这里只管理会话元数据
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ChatSessionController {

    /** 会话服务，负责会话的CRUD操作 */
    private final ChatSessionService chatSessionService;

    /** 消息服务，负责消息的查询和删除 */
    private final ChatMessageService chatMessageService;

    /**
     * 创建新会话
     *
     * 请求：POST /api/sessions
     * 请求体：{"title": "我的对话"}（可选，默认为"新对话"）
     * 响应：{"code": 200, "message": "会话创建成功", "data": {...}}
     *
     * @param request 创建会话请求（可选，包含标题）
     * @return 创建成功的会话
     */
    @PostMapping
    public ApiResponse<ChatSession> createSession(@RequestBody(required = false) CreateSessionRequest request) {
        // 如果没有传标题，默认为"新对话"
        String title = (request != null && request.getTitle() != null) ? request.getTitle() : "新对话";
        ChatSession session = chatSessionService.createSession(title);
        return ApiResponse.success("会话创建成功", session);
    }

    /**
     * 获取所有会话列表
     *
     * 请求：GET /api/sessions
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * @return 会话列表，按创建时间降序排列
     */
    @GetMapping
    public ApiResponse<List<ChatSession>> getAllSessions() {
        List<ChatSession> sessions = chatSessionService.getAllSessions();
        return ApiResponse.success(sessions);
    }

    /**
     * 获取指定会话详情
     *
     * 请求：GET /api/sessions/{sessionId}
     * 响应：{"code": 200, "data": {...}}
     *
     * @param sessionId 会话ID
     * @return 会话详情，不存在则返回404错误
     */
    @GetMapping("/{sessionId}")
    public ApiResponse<ChatSession> getSession(@PathVariable String sessionId) {
        ChatSession session = chatSessionService.getSession(sessionId);
        if (session == null) {
            return ApiResponse.error(404, "会话不存在");
        }
        return ApiResponse.success(session);
    }

    /**
     * 获取指定会话的所有消息
     *
     * 请求：GET /api/sessions/{sessionId}/messages
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * @param sessionId 会话ID
     * @return 消息列表，按时间升序排列
     */
    @GetMapping("/{sessionId}/messages")
    public ApiResponse<List<ChatMessageEntity>> getSessionMessages(@PathVariable String sessionId) {
        List<ChatMessageEntity> messages = chatMessageService.getSessionMessages(sessionId);
        return ApiResponse.success(messages);
    }

    /**
     * 更新会话标题
     *
     * 请求：PUT /api/sessions/{sessionId}
     * 请求体：{"title": "新标题"}
     * 响应：{"code": 200, "message": "会话更新成功", "data": {...}}
     *
     * @param sessionId 会话ID
     * @param request 更新会话请求（包含新标题）
     * @return 更新后的会话，不存在则返回404错误
     */
    @PutMapping("/{sessionId}")
    public ApiResponse<ChatSession> updateSession(@PathVariable String sessionId,
                                                   @RequestBody UpdateSessionRequest request) {
        ChatSession session = chatSessionService.getSession(sessionId);
        if (session == null) {
            return ApiResponse.error(404, "会话不存在");
        }
        session = chatSessionService.updateSessionTitle(sessionId, request.getTitle());
        return ApiResponse.success("会话更新成功", session);
    }

    /**
     * 删除会话及其所有消息
     *
     * 请求：DELETE /api/sessions/{sessionId}
     * 响应：{"code": 200, "message": "会话删除成功"}
     *
     * 注意：会同时删除该会话下的所有消息
     *
     * @param sessionId 会话ID
     * @return 删除成功响应，不存在则返回404错误
     */
    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> deleteSession(@PathVariable String sessionId) {
        ChatSession session = chatSessionService.getSession(sessionId);
        if (session == null) {
            return ApiResponse.error(404, "会话不存在");
        }
        // 删除会话及其消息
        chatMessageService.deleteSessionMessages(sessionId);
        chatSessionService.deleteSession(sessionId);
        return ApiResponse.success("会话删除成功", null);
    }

    /**
     * 创建会话请求体
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSessionRequest {
        /** 会话标题（可选，默认为"新对话"） */
        private String title;
    }

    /**
     * 更新会话请求体
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSessionRequest {
        /** 新的会话标题 */
        private String title;
    }
}
