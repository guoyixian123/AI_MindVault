package org.example.major_ai.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.aiservice.ConsultantService;
import org.example.major_ai.dto.MedicineQueryRequest;
import org.example.major_ai.dto.SymptomCheckRequest;
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

    // 通用聊天（保留原有功能）
    @PostMapping(produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestBody ChatRequest request) {
        String memoryId = request.getMemoryId();
        if (memoryId == null || memoryId.isBlank()) {
            memoryId = "default";
        }

        // 确保 session 存在（外键约束要求）
        String sessionId = memoryId;
        var session = chatSessionService.getSession(sessionId);
        if (session == null) {
            chatSessionService.createSessionWithId(sessionId, "新对话");
            session = chatSessionService.getSession(sessionId);
        }

        // 保存用户消息
        chatMessageService.saveMessage(sessionId, "user", request.getMessage());

        // 更新会话标题（如果是新会话）
        if (session != null && "新对话".equals(session.getTitle())) {
            String title = request.getMessage().substring(0, Math.min(20, request.getMessage().length()));
            if (request.getMessage().length() > 20) title += "...";
            chatSessionService.updateSessionTitle(sessionId, title);
        }

        return consultantService.chat(memoryId, request.getMessage());
    }

    // ==================== 用药智能咨询 ====================

    @PostMapping(value = "/medicine", produces = "text/html;charset=utf-8")
    public Flux<String> medicineConsult(@RequestBody MedicineQueryRequest request) {
        String prompt = buildMedicinePrompt(request);
        String memoryId = "medicine-" + System.currentTimeMillis();
        return consultantService.chat(memoryId, prompt);
    }

    private String buildMedicinePrompt(MedicineQueryRequest request) {
        return switch (request.getScenario()) {
            case "query" -> "请查询以下药品的详细信息，包含功效、适应症、标准用法用量、禁忌人群、常见不良反应、保质期与储存条件：\n" +
                    "药品名称：" + request.getMedicineName();

            case "interaction" -> "请检测以下多种药物的联用风险，重点分析药物相互作用、肝肾损伤风险，标注联用禁忌与注意事项：\n" +
                    "药品列表：" + String.join("、", request.getMedicineNames());

            case "special" -> "请针对以下特殊人群，分析药品" + request.getMedicineName() +
                    "的用药提示，包括禁用/慎用清单与剂量调整建议：\n" +
                    "特殊人群：" + request.getPopulation();

            case "emergency" -> "请提供药品" + request.getMedicineName() +
                    "的用药应急处理方案，包括漏服、误服后的通用应急处理措施，处方药异常情况需明确标注立即就医。";

            default -> "请提供药品" + request.getMedicineName() + "的详细用药指导。";
        };
    }

    // ==================== 疾病科普答疑 ====================

    @PostMapping(value = "/disease", produces = "text/html;charset=utf-8")
    public Flux<String> diseaseKnowledge(@RequestBody DiseaseRequest request) {
        String prompt = switch (request.getScenario()) {
            case "knowledge" -> "请用通俗易懂的语言科普以下疾病知识，包含发病原理、典型临床表现、病程发展规律与常规干预原则：\n" +
                    "疾病名称：" + request.getDiseaseName();

            case "compare" -> "请对比鉴别以下两种易混淆的病症，明确核心差异点，帮助降低自行误判概率：\n" +
                    "病症A：" + request.getDiseaseName() + "\n病症B：" + request.getCompareWith();

            case "debunk" -> "请针对以下健康说法进行科学辟谣，给出权威依据与正确认知：\n" +
                    "说法内容：" + request.getClaim();

            default -> "请科普以下疾病知识：" + request.getDiseaseName();
        };

        String memoryId = "disease-" + System.currentTimeMillis();
        return consultantService.chat(memoryId, prompt);
    }

    // ==================== 症状自查辅助 ====================

    @PostMapping(value = "/symptom", produces = "text/html;charset=utf-8")
    public Flux<String> symptomCheck(@RequestBody SymptomCheckRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下症状信息进行健康自查分析，给出分层风险分级判断（轻症/中症/高危）及对应建议：\n\n");
        prompt.append("【症状分类】").append(request.getCategory()).append("\n");
        prompt.append("【症状描述】").append(request.getDescription()).append("\n");

        if (request.getDuration() != null) {
            prompt.append("【持续时间】").append(request.getDuration()).append("天\n");
        }
        if (request.getPainLevel() != null) {
            prompt.append("【痛感等级】").append(request.getPainLevel()).append("/10\n");
        }
        if (request.getAccompanying() != null) {
            prompt.append("【伴随症状】").append(request.getAccompanying()).append("\n");
        }
        if (request.getTriggers() != null) {
            prompt.append("【诱发因素】").append(request.getTriggers()).append("\n");
        }
        if (request.getHistory() != null) {
            prompt.append("【既往病史】").append(request.getHistory()).append("\n");
        }
        if (request.getAllergies() != null) {
            prompt.append("【药物过敏史】").append(request.getAllergies()).append("\n");
        }

        String population = request.getPopulation() != null ? request.getPopulation() : "成人";
        prompt.append("【人群类型】").append(population).append("\n\n");

        prompt.append("请按照以下格式输出：\n");
        prompt.append("1. 风险等级：[轻症/中症/高危]\n");
        prompt.append("2. 初步分析\n");
        prompt.append("3. 处理建议\n");
        prompt.append("4. 就医指引（如需就医，推荐科室）\n");
        prompt.append("5. 注意事项\n");

        if ("高危".equals(request.getCategory()) || (request.getPainLevel() != null && request.getPainLevel() >= 8)) {
            prompt.append("\n⚠️ 注意：根据症状描述，可能存在高危风险，请务必强调需要立即就医的情况。");
        }

        String memoryId = "symptom-" + System.currentTimeMillis();
        return consultantService.chat(memoryId, prompt.toString());
    }

    // ==================== 请求DTO ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private String memoryId;
        private String message;
    }

    @Data
    public static class DiseaseRequest {
        private String diseaseName;
        private String compareWith;
        private String claim;
        private String scenario; // knowledge/compare/debunk
    }
}
