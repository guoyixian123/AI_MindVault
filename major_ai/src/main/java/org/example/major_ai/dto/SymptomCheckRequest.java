package org.example.major_ai.dto;

import lombok.Data;

/**
 * 症状自查请求DTO
 * 用于症状自查接口的请求参数
 *
 * 请求示例：
 * {
 *   "category": "头痛",
 *   "description": "持续性钝痛，集中在太阳穴",
 *   "duration": 3,
 *   "painLevel": 6,
 *   "accompanying": "恶心、畏光",
 *   "triggers": "压力大、睡眠不足",
 *   "history": "偏头痛病史",
 *   "allergies": "无",
 *   "population": "成年人"
 * }
 */
@Data
public class SymptomCheckRequest {

    /** 症状分类：发热、疼痛、咳嗽、胃肠不适、皮肤症状等 */
    private String category;

    /** 症状描述 */
    private String description;

    /** 持续时间（天） */
    private Integer duration;

    /** 痛感等级（1-10，10为最痛） */
    private Integer painLevel;

    /** 伴随症状 */
    private String accompanying;

    /** 诱发因素 */
    private String triggers;

    /** 既往病史 */
    private String history;

    /** 药物过敏史 */
    private String allergies;

    /** 人群类型：成人、儿童、老人、孕妇 */
    private String population;
}
