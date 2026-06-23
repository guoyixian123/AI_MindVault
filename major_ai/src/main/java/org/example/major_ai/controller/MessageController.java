package org.example.major_ai.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.service.ChatMessageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final ChatMessageService chatMessageService;

    @PostMapping
    public ApiResponse<Void> saveMessage(@RequestBody SaveMessageRequest request) {
        chatMessageService.saveMessage(request.getSessionId(), request.getRole(), request.getContent());
        return ApiResponse.success("消息保存成功", null);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveMessageRequest {
        private String sessionId;
        private String role;
        private String content;
    }
}
