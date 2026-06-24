package org.example.major_ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class MedicineQueryRequest {

    private String medicineName;           // 单药品查询
    private List<String> medicineNames;    // 多药品联用检测
    private String population;             // 特殊人群类型
    private String scenario;               // 场景：query/interaction/special/emergency
}
