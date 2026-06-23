package org.example.major_ai.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.aiservice.ConsultantService;
import org.example.major_ai.service.ChatMessageService;
import org.example.major_ai.service.ChatSessionService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ConsultantService consultantService;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;

    @PostMapping(produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestBody ChatRequest request) {
        String memoryId = request.getMemoryId();
        if (memoryId == null || memoryId.isBlank()) {
            memoryId = "default";
        }

        // 保存用户消息
        String sessionId = memoryId;
        chatMessageService.saveMessage(sessionId, "user", request.getMessage());

        // 更新会话标题（如果是新会话）
        var session = chatSessionService.getSession(sessionId);
        if (session != null && "新对话".equals(session.getTitle())) {
            String title = request.getMessage().substring(0, Math.min(20, request.getMessage().length()));
            if (request.getMessage().length() > 20) title += "...";
            chatSessionService.updateSessionTitle(sessionId, title);
        }

        return consultantService.chat(memoryId, request.getMessage());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private String memoryId;
        private String message;
    }
}
