package org.example.major_ai.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.major_ai.aiservice.ConsultantService;
import org.example.major_ai.service.ChatMessageService;
import org.example.major_ai.service.ChatSessionService;
import org.example.major_ai.service.HealthReportService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket消息处理器
 * 负责接收前端消息，根据场景分发到不同的AI处理逻辑，流式返回结果
 *
 * 支持的场景(scenario)：
 * - chat: 普通对话
 * - symptom: 症状自查
 * - medicine: 药品查询/联用检测
 * - disease: 疾病科普/对比/辟谣
 * - report: 体检报告解读
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    /** AI服务接口，调用LangChain4j进行对话 */
    private final ConsultantService consultantService;

    /** 聊天消息服务，负责消息的持久化存储 */
    private final ChatMessageService chatMessageService;

    /** 聊天会话服务，负责会话的CRUD操作 */
    private final ChatSessionService chatSessionService;

    /** 健康报告服务，用于体检报告分析结果的回写 */
    private final HealthReportService healthReportService;

    /** JSON解析器，用于解析前端发送的JSON消息 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 当前活跃的WebSocket会话列表，使用线程安全的CopyOnWriteArrayList */
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    /**
     * WebSocket连接建立时的回调
     * 将新连接加入会话列表，便于后续管理和广播
     *
     * @param session 新建立的WebSocket会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket 连接建立: {}", session.getId());
    }

    /**
     * WebSocket连接关闭时的回调
     * 从会话列表中移除已关闭的连接，防止内存泄漏
     *
     * @param session 关闭的WebSocket会话
     * @param status  关闭状态码
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket 连接关闭: {}", session.getId());
    }

    /**
     * 处理前端发送的文本消息
     * 这是核心方法，负责：
     * 1. 解析JSON消息
     * 2. 根据场景构建prompt
     * 3. 调用AI服务
     * 4. 流式返回结果
     * 5. 保存聊天记录
     *
     * @param session 当前WebSocket会话
     * @param message 前端发送的文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // ========== 1. 解析前端JSON消息 ==========
            String payload = message.getPayload();
            log.info("收到原始消息: {}", payload);
            JsonNode json = objectMapper.readTree(payload);

            // 提取会话ID（用于隔离不同用户的聊天记忆）
            String rawMemoryId = json.has("memoryId") ? json.get("memoryId").asText() : "default";
            String memoryId = rawMemoryId.isBlank() ? "default" : rawMemoryId;

            // 提取用户消息内容
            String userMessage = json.has("message") ? json.get("message").asText() : "";

            // 提取场景标识（chat/symptom/medicine/disease/report）
            String scenario = json.has("scenario") ? json.get("scenario").asText() : "chat";

            // 提取体检报告ID（仅report场景使用，用于回写分析结果）
            Long reportId = json.has("reportId") && !json.get("reportId").isNull()
                    ? json.get("reportId").asLong() : null;

            log.info("解析后: memoryId={}, scenario={}, message='{}', reportId={}", memoryId, scenario, userMessage, reportId);

            // ========== 2. 根据场景构建AI提示词(prompt) ==========
            // 不同场景需要不同的prompt格式，让AI理解用户意图
            String prompt = switch (scenario) {
                case "symptom" -> buildSymptomPrompt(json);   // 症状自查：拼接症状信息
                case "medicine" -> buildMedicinePrompt(json); // 药品查询：根据子场景分发
                case "disease" -> buildDiseasePrompt(json);   // 疾病科普：根据子场景分发
                case "report" -> buildReportPrompt(json);     // 体检报告：构建报告解读指令
                default -> userMessage;                        // 普通对话：直接使用用户消息
            };

            // ========== 3. 空消息检查 ==========
            // 仅对chat场景进行空消息检查，其他场景可能没有message字段
            if ("chat".equals(scenario) && userMessage.isBlank()) {
                log.warn("收到空消息，返回提示");
                sendMessage(session, "请输入您的问题");
                sendMessage(session, "[DONE]");  // 发送结束标记，通知前端回复完成
                return;
            }

            // ========== 4. 确保会话存在 ==========
            // 数据库外键约束要求：保存消息前必须先有对应的会话记录
            var sessionInfo = chatSessionService.getSession(memoryId);
            if (sessionInfo == null) {
                log.info("会话不存在，创建新会话: {}", memoryId);
                chatSessionService.createSessionWithId(memoryId, "新对话");
                sessionInfo = chatSessionService.getSession(memoryId);
            }

            // ========== 5. 保存用户消息到数据库 ==========
            // 非chat场景（如symptom/medicine）没有message字段，用场景名替代
            String saveMessage = userMessage.isBlank() ? "[" + scenario + "查询]" : userMessage;
            chatMessageService.saveMessage(memoryId, "user", saveMessage);

            // ========== 6. 更新会话标题 ==========
            // 首次对话时，自动用用户消息的前20个字符作为会话标题
            // 方便用户在会话列表中识别每个对话
            if (sessionInfo != null && "新对话".equals(sessionInfo.getTitle())) {
                String title = saveMessage.substring(0, Math.min(20, saveMessage.length()));
                if (saveMessage.length() > 20) title += "...";
                chatSessionService.updateSessionTitle(memoryId, title);
            }

            // ========== 7. 调用AI服务并流式返回结果 ==========
            StringBuilder fullReply = new StringBuilder();  // 用于拼接完整的AI回复
            consultantService.chat(memoryId, prompt)
                    // 阶段1：每收到一个token（文字片段），立即发送给前端
                    // 实现打字机效果，用户无需等待完整回复
                    .doOnNext(chunk -> {
                        fullReply.append(chunk);       // 拼接到完整回复
                        sendMessage(session, chunk);   // 实时发送给前端
                    })
                    // 阶段2：AI回复完成
                    .doOnComplete(() -> {
                        sendMessage(session, "[DONE]");  // 发送结束标记
                        // 保存完整的AI回复到数据库
                        if (!fullReply.isEmpty()) {
                            chatMessageService.saveMessage(memoryId, "assistant", fullReply.toString());
                            // 体检报告场景特殊处理：将AI分析结果回写到health_report表
                            // 这样用户下次查看报告时能看到之前的解读
                            if ("report".equals(scenario) && reportId != null) {
                                healthReportService.updateAnalysis(reportId, fullReply.toString());
                                log.info("体检报告分析结果已保存: reportId={}", reportId);
                            }
                        }
                    })
                    // 阶段3：AI调用出错
                    .doOnError(error -> {
                        log.error("AI 回复出错", error);
                        sendMessage(session, "处理出错: " + error.getMessage());  // 发送错误信息
                        sendMessage(session, "[DONE]");  // 仍然发送结束标记，保证前端能正常关闭
                    })
                    .subscribe();  // 触发订阅，开始执行流式调用

        } catch (Exception e) {
            // JSON解析失败或其他异常
            log.error("WebSocket 处理异常", e);
            sendMessage(session, "处理出错: " + e.getMessage());
            sendMessage(session, "[DONE]");
        }
    }

    /**
     * 发送消息给前端
     * 包装了异常处理，防止发送失败导致程序崩溃
     *
     * @param session WebSocket会话
     * @param content 要发送的文本内容
     */
    private void sendMessage(WebSocketSession session, String content) {
        try {
            // 检查连接是否仍然打开，避免向已关闭的连接发送消息
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(content));
            }
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }

    /**
     * 构建症状自查场景的prompt
     * 将前端的结构化症状数据转换为AI能理解的自然语言描述
     *
     * 前端发送格式：
     * {
     *   "scenario": "symptom",
     *   "category": "头痛",
     *   "description": "持续性钝痛",
     *   "duration": "3",
     *   "painLevel": "6",
     *   "accompanying": "恶心",
     *   "population": "成年人"
     * }
     *
     * @param json 前端发送的JSON数据
     * @return 构建好的prompt字符串
     */
    private String buildSymptomPrompt(JsonNode json) {
        // 拼接prompt头部，指导AI进行风险分级判断
        StringBuilder prompt = new StringBuilder("请根据以下症状信息进行健康自查分析，给出分层风险分级判断及对应建议：\n\n");
        // 逐个提取症状字段，拼接到prompt中
        if (json.has("category")) prompt.append("【症状分类】").append(json.get("category").asText()).append("\n");
        if (json.has("description")) prompt.append("【症状描述】").append(json.get("description").asText()).append("\n");
        if (json.has("duration")) prompt.append("【持续时间】").append(json.get("duration").asText()).append("天\n");
        if (json.has("painLevel")) prompt.append("【痛感等级】").append(json.get("painLevel").asText()).append("/10\n");
        if (json.has("accompanying")) prompt.append("【伴随症状】").append(json.get("accompanying").asText()).append("\n");
        if (json.has("population")) prompt.append("【人群类型】").append(json.get("population").asText()).append("\n");
        return prompt.toString();
    }

    /**
     * 构建药品查询场景的prompt
     * 根据子场景(subScenario)分发到不同的处理逻辑：
     * - query: 查询药品详细信息
     * - interaction: 药物联用风险检测
     * - special: 特殊人群用药提示
     * - emergency: 用药应急处理方案
     *
     * @param json 前端发送的JSON数据
     * @return 构建好的prompt字符串
     */
    private String buildMedicinePrompt(JsonNode json) {
        // 提取子场景标识，默认为"query"
        String subScenario = json.has("subScenario") ? json.get("subScenario").asText() : "query";
        // 提取药品名称
        String name = json.has("medicineName") ? json.get("medicineName").asText() : "";
        // 根据子场景返回不同的prompt
        return switch (subScenario) {
            case "query" -> "请查询以下药品的详细信息：\n药品名称：" + name;
            case "interaction" -> "请检测以下药物联用风险：\n" + (json.has("medicineNames") ? json.get("medicineNames").toString() : "");
            case "special" -> "请针对" + (json.has("population") ? json.get("population").asText() : "") + "分析药品" + name + "的用药提示";
            case "emergency" -> "请提供药品" + name + "的用药应急处理方案";
            default -> "请提供药品" + name + "的详细用药指导";
        };
    }

    /**
     * 构建疾病科普场景的prompt
     * 根据子场景(subScenario)分发到不同的处理逻辑：
     * - knowledge: 疾病知识科普
     * - compare: 两种疾病的对比鉴别
     * - debunk: 辟谣（验证某个健康说法是否正确）
     *
     * @param json 前端发送的JSON数据
     * @return 构建好的prompt字符串
     */
    private String buildDiseasePrompt(JsonNode json) {
        // 提取子场景标识，默认为"knowledge"
        String subScenario = json.has("subScenario") ? json.get("subScenario").asText() : "knowledge";
        // 提取疾病名称
        String name = json.has("diseaseName") ? json.get("diseaseName").asText() : "";
        // 根据子场景返回不同的prompt
        return switch (subScenario) {
            case "knowledge" -> "请科普以下疾病知识：" + name;
            case "compare" -> "请对比鉴别：" + name + " vs " + (json.has("compareWith") ? json.get("compareWith").asText() : "");
            case "debunk" -> "请辟谣：" + (json.has("claim") ? json.get("claim").asText() : "");
            default -> "请科普：" + name;
        };
    }

    /**
     * 构建体检报告解读场景的prompt
     * 指导AI按照专业格式解读体检报告：
     * 1. 逐项解读指标含义
     * 2. 标注异常指标（↑偏高 ↓偏低）
     * 3. 解释异常原因
     * 4. 给出改善建议
     * 5. 提示需要复查的项目
     *
     * @param json 前端发送的JSON数据
     * @return 构建好的prompt字符串
     */
    private String buildReportPrompt(JsonNode json) {
        // 提取报告类型，默认为"综合体检报告"
        String reportType = json.has("reportType") ? json.get("reportType").asText() : "综合体检报告";
        // 提取报告内容
        String content = json.has("reportContent") ? json.get("reportContent").asText() : "";
        // 构建详细的解读指令，指导AI按照5个步骤进行解读
        return "你是一位专业的体检报告解读顾问。请用通俗易懂的语言解读以下体检报告，要求：\n" +
                "1. 逐项解读每个指标，说明其含义\n" +
                "2. 标注异常指标（偏高用↑，偏低用↓）\n" +
                "3. 解释异常指标可能的原因\n" +
                "4. 给出通用的改善建议\n" +
                "5. 提示哪些指标需要复查或进一步检查\n\n" +
                "报告类型：" + reportType + "\n" +
                "报告内容：\n" + content;
    }
}
