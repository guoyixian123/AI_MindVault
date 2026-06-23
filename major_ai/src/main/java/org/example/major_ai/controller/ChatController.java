package org.example.major_ai.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.aiservice.ConsultantService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ConsultantService consultantService;

    @PostMapping(produces = "text/html;charset=utf-8")
    public Flux<String> chat(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        // 使用认证用户的ID作为会话隔离的基础
        String userId = (String) authentication.getPrincipal();
        String memoryId = request.getMemoryId();
        if (memoryId == null || memoryId.isBlank()) {
            memoryId = userId; // 默认使用用户ID作为会话ID
        }
        return consultantService.chat(memoryId, request.getMessage());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private String memoryId;

        @NotBlank(message = "消息不能为空")
        @Size(max = 2000, message = "消息长度不能超过2000个字符")
        private String message;
    }
}
