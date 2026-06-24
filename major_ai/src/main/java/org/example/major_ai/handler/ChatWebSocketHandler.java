package org.example.major_ai.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.major_ai.aiservice.ConsultantService;
import org.example.major_ai.service.ChatMessageService;
import org.example.major_ai.service.ChatSessionService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ConsultantService consultantService;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket 连接建立: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket 连接关闭: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.info("收到原始消息: {}", payload);
            JsonNode json = objectMapper.readTree(payload);
            String rawMemoryId = json.has("memoryId") ? json.get("memoryId").asText() : "default";
            String memoryId = rawMemoryId.isBlank() ? "default" : rawMemoryId;
            String userMessage = json.has("message") ? json.get("message").asText() : "";
            String scenario = json.has("scenario") ? json.get("scenario").asText() : "chat";

            log.info("解析后: memoryId={}, scenario={}, message='{}'", memoryId, scenario, userMessage);

            // 根据场景构建 prompt
            String prompt = switch (scenario) {
                case "symptom" -> buildSymptomPrompt(json);
                case "medicine" -> buildMedicinePrompt(json);
                case "disease" -> buildDiseasePrompt(json);
                default -> userMessage;
            };

            // 空消息检查（仅对 chat 场景）
            if ("chat".equals(scenario) && userMessage.isBlank()) {
                log.warn("收到空消息，返回提示");
                sendMessage(session, "请输入您的问题");
                sendMessage(session, "[DONE]");
                return;
            }

            // 确保 session 存在（外键约束要求）
            var sessionInfo = chatSessionService.getSession(memoryId);
            if (sessionInfo == null) {
                log.info("会话不存在，创建新会话: {}", memoryId);
                chatSessionService.createSessionWithId(memoryId, "新对话");
                sessionInfo = chatSessionService.getSession(memoryId);
            }

            // 保存用户消息
            String saveMessage = userMessage.isBlank() ? "[" + scenario + "查询]" : userMessage;
            chatMessageService.saveMessage(memoryId, "user", saveMessage);

            // 更新会话标题
            if (sessionInfo != null && "新对话".equals(sessionInfo.getTitle())) {
                String title = saveMessage.substring(0, Math.min(20, saveMessage.length()));
                if (saveMessage.length() > 20) title += "...";
                chatSessionService.updateSessionTitle(memoryId, title);
            }

            // 流式发送 AI 回复
            StringBuilder fullReply = new StringBuilder();
            consultantService.chat(memoryId, prompt)
                    .doOnNext(chunk -> {
                        fullReply.append(chunk);
                        sendMessage(session, chunk);
                    })
                    .doOnComplete(() -> {
                        sendMessage(session, "[DONE]");
                        if (!fullReply.isEmpty()) {
                            chatMessageService.saveMessage(memoryId, "assistant", fullReply.toString());
                        }
                    })
                    .doOnError(error -> {
                        log.error("AI 回复出错", error);
                        sendMessage(session, "处理出错: " + error.getMessage());
                        sendMessage(session, "[DONE]");
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("WebSocket 处理异常", e);
            sendMessage(session, "处理出错: " + e.getMessage());
            sendMessage(session, "[DONE]");
        }
    }

    private void sendMessage(WebSocketSession session, String content) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(content));
            }
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }

    private String buildSymptomPrompt(JsonNode json) {
        StringBuilder prompt = new StringBuilder("请根据以下症状信息进行健康自查分析，给出分层风险分级判断及对应建议：\n\n");
        if (json.has("category")) prompt.append("【症状分类】").append(json.get("category").asText()).append("\n");
        if (json.has("description")) prompt.append("【症状描述】").append(json.get("description").asText()).append("\n");
        if (json.has("duration")) prompt.append("【持续时间】").append(json.get("duration").asText()).append("天\n");
        if (json.has("painLevel")) prompt.append("【痛感等级】").append(json.get("painLevel").asText()).append("/10\n");
        if (json.has("accompanying")) prompt.append("【伴随症状】").append(json.get("accompanying").asText()).append("\n");
        if (json.has("population")) prompt.append("【人群类型】").append(json.get("population").asText()).append("\n");
        return prompt.toString();
    }

    private String buildMedicinePrompt(JsonNode json) {
        String subScenario = json.has("subScenario") ? json.get("subScenario").asText() : "query";
        String name = json.has("medicineName") ? json.get("medicineName").asText() : "";
        return switch (subScenario) {
            case "query" -> "请查询以下药品的详细信息：\n药品名称：" + name;
            case "interaction" -> "请检测以下药物联用风险：\n" + (json.has("medicineNames") ? json.get("medicineNames").toString() : "");
            case "special" -> "请针对" + (json.has("population") ? json.get("population").asText() : "") + "分析药品" + name + "的用药提示";
            case "emergency" -> "请提供药品" + name + "的用药应急处理方案";
            default -> "请提供药品" + name + "的详细用药指导";
        };
    }

    private String buildDiseasePrompt(JsonNode json) {
        String subScenario = json.has("subScenario") ? json.get("subScenario").asText() : "knowledge";
        String name = json.has("diseaseName") ? json.get("diseaseName").asText() : "";
        return switch (subScenario) {
            case "knowledge" -> "请科普以下疾病知识：" + name;
            case "compare" -> "请对比鉴别：" + name + " vs " + (json.has("compareWith") ? json.get("compareWith").asText() : "");
            case "debunk" -> "请辟谣：" + (json.has("claim") ? json.get("claim").asText() : "");
            default -> "请科普：" + name;
        };
    }
}
