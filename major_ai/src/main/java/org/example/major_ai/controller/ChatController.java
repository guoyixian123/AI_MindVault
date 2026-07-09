package org.example.major_ai.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.major_ai.aiservice.ConsultantService;
import org.example.major_ai.service.ChatMessageService;
import org.example.major_ai.service.ChatSessionService;
import org.example.major_ai.service.HealthReportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI聊天流式接口
 * 使用Flux<String>实现流式返回
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ConsultantService consultantService;
    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final HealthReportService healthReportService;
    private final ChatMemoryStore chatMemoryStore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> stream(@RequestBody String body) {
        try {
            JsonNode json = objectMapper.readTree(body);

            String rawMemoryId = json.has("memoryId") ? json.get("memoryId").asText() : "default";
            String memoryId = rawMemoryId.isBlank() ? "default" : rawMemoryId;
            String userMessage = json.has("message") ? json.get("message").asText() : "";
            String scenario = json.has("scenario") ? json.get("scenario").asText() : "chat";
            Long reportId = json.has("reportId") && !json.get("reportId").isNull()
                    ? json.get("reportId").asLong() : null;

            log.info("流式聊天: memoryId={}, scenario={}, message='{}', reportId={}", memoryId, scenario, userMessage, reportId);

            String prompt = switch (scenario) {
                case "symptom" -> buildSymptomPrompt(json);
                case "medicine" -> buildMedicinePrompt(json);
                case "disease" -> buildDiseasePrompt(json);
                case "report" -> buildReportPrompt(json);
                default -> buildFollowUpPrompt(memoryId, userMessage);
            };

            if ("chat".equals(scenario) && prompt.isBlank()) {
                return Flux.just("请输入您的问题");
            }

            var sessionInfo = chatSessionService.getSession(memoryId);
            if (sessionInfo == null) {
                chatSessionService.createSessionWithId(memoryId, "新对话");
                sessionInfo = chatSessionService.getSession(memoryId);
            }

            String saveMessage = userMessage.isBlank()
                    ? buildHistoryLabel(scenario, json)
                    : userMessage;
            chatMessageService.saveMessage(memoryId, "user", saveMessage);

            if (sessionInfo != null && "新对话".equals(sessionInfo.getTitle())) {
                String title = buildSessionTitle(scenario, json);
                chatSessionService.updateSessionTitle(memoryId, title);
            }

            StringBuilder fullReply = new StringBuilder();
            return consultantService.chat(memoryId, prompt)
                    .doOnNext(fullReply::append)
                    .doOnComplete(() -> {
                        if (!fullReply.isEmpty()) {
                            chatMessageService.saveMessage(memoryId, "assistant", fullReply.toString());
                            if ("report".equals(scenario) && reportId != null) {
                                healthReportService.updateAnalysis(reportId, fullReply.toString());
                                log.info("体检报告分析结果已保存: reportId={}", reportId);
                            }
                        }
                    })
                    .doOnError(error -> log.error("AI 回复出错", error));

        } catch (Exception e) {
            log.error("流式聊天处理异常", e);
            return Flux.just("处理出错: " + e.getMessage());
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

    private String buildReportPrompt(JsonNode json) {
        String reportType = json.has("reportType") ? json.get("reportType").asText() : "综合体检报告";
        String content = json.has("reportContent") ? json.get("reportContent").asText() : "";
        return "你是一位专业的体检报告解读顾问。请用通俗易懂的语言解读以下体检报告，要求：\n" +
                "1. 逐项解读每个指标，说明其含义\n" +
                "2. 标注异常指标（偏高用↑，偏低用↓）\n" +
                "3. 解释异常指标可能的原因\n" +
                "4. 给出通用的改善建议\n" +
                "5. 提示哪些指标需要复查或进一步检查\n\n" +
                "报告类型：" + reportType + "\n" +
                "报告内容：\n" + content;
    }

    /**
     * 构建追问的Prompt，从 Redis 加载历史对话上下文
     * Redis 中存储的是 LangChain4j 管理的真实对话（原始 prompt + AI 回复），
     * 比 MySQL 中的标签更准确
     */
    /**
     * 为初始查询构建有意义的历史记录标签（替代丑陋的 [xxx查询]）
     */
    private String buildSessionTitle(String scenario, JsonNode json) {
        return switch (scenario) {
            case "medicine" -> {
                String sub = json.has("subScenario") ? json.get("subScenario").asText() : "query";
                yield switch (sub) {
                    case "query" -> "药品查询";
                    case "interaction" -> "联用检测";
                    case "special" -> "特殊人群用药";
                    case "emergency" -> "应急处理";
                    default -> "用药咨询";
                };
            }
            case "disease" -> {
                String sub = json.has("subScenario") ? json.get("subScenario").asText() : "knowledge";
                yield switch (sub) {
                    case "knowledge" -> "疾病科普";
                    case "compare" -> "病症鉴别";
                    case "debunk" -> "谣言辟谣";
                    default -> "疾病查询";
                };
            }
            case "symptom" -> "症状自查";
            case "report" -> "体检报告";
            default -> "AI 对话";
        };
    }

    private String buildHistoryLabel(String scenario, JsonNode json) {
        return switch (scenario) {
            case "medicine" -> {
                String sub = json.has("subScenario") ? json.get("subScenario").asText() : "query";
                String name = json.has("medicineName") ? json.get("medicineName").asText() : "";
                String type = switch (sub) {
                    case "query" -> "药品查询";
                    case "interaction" -> "联用检测";
                    case "special" -> "特殊人群用药";
                    case "emergency" -> "应急处理";
                    default -> "用药咨询";
                };
                yield name.isBlank() ? type : type + "：" + name;
            }
            case "disease" -> {
                String sub = json.has("subScenario") ? json.get("subScenario").asText() : "knowledge";
                String name = json.has("diseaseName") ? json.get("diseaseName").asText() : "";
                String type = switch (sub) {
                    case "knowledge" -> "疾病科普";
                    case "compare" -> "病症鉴别";
                    case "debunk" -> "谣言辟谣";
                    default -> "疾病查询";
                };
                yield name.isBlank() ? type : type + "：" + name;
            }
            case "symptom" -> {
                String category = json.has("category") ? json.get("category").asText() : "";
                yield category.isBlank() ? "症状自查" : "症状自查：" + category;
            }
            case "report" -> {
                String rt = json.has("reportType") ? json.get("reportType").asText() : "体检报告";
                yield "体检报告：" + rt;
            }
            default -> "AI 对话";
        };
    }

    private String buildFollowUpPrompt(String memoryId, String userMessage) {
        List<ChatMessage> messages = chatMemoryStore.getMessages(memoryId);

        if (messages == null || messages.isEmpty()) {
            return userMessage;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("以下是之前的对话记录，请基于这些上下文回答用户的追问：\n\n");
        sb.append("=== 对话历史 ===\n");
        for (ChatMessage msg : messages) {
            switch (msg.type()) {
                case USER -> sb.append("用户：").append(((UserMessage) msg).singleText()).append("\n");
                case AI -> sb.append("A.R.I.A：").append(((AiMessage) msg).text()).append("\n");
                default -> { /* 跳过 SystemMessage */ }
            }
        }
        sb.append("=== 对话历史结束 ===\n\n");
        sb.append("用户的新问题：").append(userMessage);
        sb.append("\n\n请基于以上对话历史回答用户的新问题。");

        return sb.toString();
    }
}