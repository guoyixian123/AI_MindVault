package org.example.major_ai.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.ChatSession;
import org.example.major_ai.service.ChatSessionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @PostMapping
    public ApiResponse<ChatSession> createSession(@Valid @RequestBody CreateSessionRequest request,
                                                   Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        ChatSession session = chatSessionService.createSession(userId, request.getTitle());
        return ApiResponse.success("会话创建成功", session);
    }

    @GetMapping
    public ApiResponse<List<ChatSession>> getUserSessions(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        List<ChatSession> sessions = chatSessionService.getUserSessions(userId);
        return ApiResponse.success(sessions);
    }

    @PutMapping("/{sessionId}")
    public ApiResponse<ChatSession> updateSession(@PathVariable String sessionId,
                                                   @Valid @RequestBody UpdateSessionRequest request,
                                                   Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        // 验证会话属于当前用户
        ChatSession session = chatSessionService.getSession(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return ApiResponse.error(403, "无权访问此会话");
        }
        session = chatSessionService.updateSessionTitle(sessionId, request.getTitle());
        return ApiResponse.success("会话更新成功", session);
    }

    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> deleteSession(@PathVariable String sessionId,
                                            Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        // 验证会话属于当前用户
        ChatSession session = chatSessionService.getSession(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return ApiResponse.error(403, "无权访问此会话");
        }
        chatSessionService.deleteSession(userId, sessionId);
        return ApiResponse.success("会话删除成功", null);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSessionRequest {
        @Size(max = 100, message = "会话标题长度不能超过100个字符")
        private String title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateSessionRequest {
        @Size(max = 100, message = "会话标题长度不能超过100个字符")
        private String title;
    }
}
