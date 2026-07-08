package org.example.major_ai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应包装类
 * 所有REST API的响应都使用这个类包装，保证响应格式一致
 *
 * 响应格式：
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": {...}
 * }
 *
 * 使用示例：
 * - 成功：ApiResponse.success(data)
 * - 成功带消息：ApiResponse.success("创建成功", data)
 * - 错误：ApiResponse.error(404, "用户不存在")
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    /** 状态码：200=成功，4xx=客户端错误，5xx=服务器错误 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /**
     * 成功响应（默认消息）
     *
     * @param data 响应数据
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param message 响应消息
     * @param data 响应数据
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 错误响应（指定状态码）
     *
     * @param code 错误码
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 错误响应（默认500服务器错误）
     *
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
}
