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

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    @PostMapping
    public ApiResponse<ChatSession> createSession(@RequestBody(required = false) CreateSessionRequest request) {
        String title = (request != null && request.getTitle() != null) ? request.getTitle() : "新对话";
        ChatSession session = chatSessionService.createSession(title);
        return ApiResponse.success("会话创建成功", session);
    }

    @GetMapping
    public ApiResponse<List<ChatSession>> getAllSessions() {
        List<ChatSession> sessions = chatSessionService.getAllSessions();
        return ApiResponse.success(sessions);
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<ChatSession> getSession(@PathVariable String sessionId) {
        ChatSession session = chatSessionService.getSession(sessionId);
        if (session == null) {
            return ApiResponse.error(404, "会话不存在");
        }
        return ApiResponse.success(session);
    }

    @GetMapping("/{sessionId}/messages")
    public ApiResponse<List<ChatMessageEntity>> getSessionMessages(@PathVariable String sessionId) {
        List<ChatMessageEntity> messages = chatMessageService.getSessionMessages(sessionId);
        return ApiResponse.success(messages);
    }

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSessionRequest {
        private String title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSessionRequest {
        private String title;
    }
}
