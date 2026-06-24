package org.example.major_ai.dto;

import lombok.Data;

@Data
public class SymptomCheckRequest {

    private String category;       // 症状分类：发热、疼痛、咳嗽、胃肠不适、皮肤症状等
    private String description;    // 症状描述
    private Integer duration;      // 持续时间（天）
    private Integer painLevel;     // 痛感等级 1-10
    private String accompanying;   // 伴随症状
    private String triggers;       // 诱发因素
    private String history;        // 既往病史
    private String allergies;      // 药物过敏史
    private String population;     // 人群类型：成人、儿童、老人、孕妇
}
