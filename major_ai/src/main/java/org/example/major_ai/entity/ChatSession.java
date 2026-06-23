package org.example.major_ai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    private String id;
    private String userId;
    private String title;
    private long createdAt;
    private long updatedAt;
}
