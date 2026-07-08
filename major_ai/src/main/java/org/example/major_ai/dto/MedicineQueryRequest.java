package org.example.major_ai.dto;

import lombok.Data;

import java.util.List;

/**
 * 药品查询请求DTO
 * 用于药品查询接口的请求参数
 *
 * 请求示例：
 * {
 *   "medicineName": "阿莫西林",
 *   "medicineNames": ["阿莫西林", "头孢克肟"],
 *   "population": "孕妇",
 *   "scenario": "interaction"
 * }
 */
@Data
public class MedicineQueryRequest {

    /** 单药品查询（scenario=query/special/emergency时使用） */
    private String medicineName;

    /** 多药品联用检测（scenario=interaction时使用） */
    private List<String> medicineNames;

    /** 特殊人群类型（scenario=special时使用）：儿童、老人、孕妇、哺乳期等 */
    private String population;

    /** 场景标识：
     * - query: 查询药品详细信息
     * - interaction: 药物联用风险检测
     * - special: 特殊人群用药提示
     * - emergency: 用药应急处理方案
     */
    private String scenario;
}
