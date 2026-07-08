package org.example.major_ai.controller;

import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ApiResponse;
import org.example.major_ai.entity.ConsultationPostEntity;
import org.example.major_ai.entity.DoctorReplyEntity;
import org.example.major_ai.entity.UserEntity;
import org.example.major_ai.security.UserDetailsImpl;
import org.example.major_ai.service.ConsultationService;
import org.example.major_ai.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 社区问诊控制器
 * 提供问诊帖的发布、查询、回复和关闭接口
 *
 * API前缀：/api/consultation
 *
 * 功能：
 * - 用户发布问诊帖
 * - 查询问诊帖列表
 * - 查看帖子详情（包含医生回复）
 * - 医生回复帖子
 * - 用户关闭帖子
 *
 * 注意：这是社区问诊（人工），不是AI问诊
 */
@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
public class ConsultationController {

    /** 问诊服务，负责帖子和回复的CRUD */
    private final ConsultationService consultationService;

    /** 用户服务，用于查询医生信息 */
    private final UserService userService;

    /**
     * 发布问诊帖
     *
     * 请求：POST /api/consultation/posts
     * 请求头：Authorization: Bearer {token}
     * 请求体：{"departmentId": 3, "title": "头疼三天了", "content": "..."}
     * 响应：{"code": 200, "message": "问诊帖发布成功", "data": {...}}
     *
     * @param user 当前登录用户（从JWT Token中提取）
     * @param post 帖子实体
     * @return 创建成功的帖子
     */
    @PostMapping("/posts")
    public ApiResponse<ConsultationPostEntity> createPost(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody ConsultationPostEntity post) {
        // 设置发帖用户ID
        post.setUserId(user.getId());
        ConsultationPostEntity created = consultationService.createPost(post);
        return ApiResponse.success("问诊帖发布成功", created);
    }

    /**
     * 查询问诊帖列表
     *
     * 请求：GET /api/consultation/posts?departmentId=3&status=PENDING
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * @param departmentId 科室ID（可选）
     * @param status 状态（可选：PENDING/REPLIED/CLOSED）
     * @return 帖子列表
     */
    @GetMapping("/posts")
    public ApiResponse<List<ConsultationPostEntity>> listPosts(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status) {
        List<ConsultationPostEntity> posts = consultationService.listPosts(departmentId, status);
        return ApiResponse.success(posts);
    }

    /**
     * 查看帖子详情（包含医生回复）
     *
     * 请求：GET /api/consultation/posts/{id}
     * 响应：{"code": 200, "data": {"post": {...}, "replies": [{...}]}}
     *
     * @param id 帖子ID
     * @return 帖子详情和回复列表
     */
    @GetMapping("/posts/{id}")
    public ApiResponse<Map<String, Object>> getPostDetail(
            @PathVariable Long id) {
        // 查询帖子
        ConsultationPostEntity post = consultationService.findPostById(id);
        if (post == null) {
            return ApiResponse.error(404, "帖子不存在");
        }

        // 查询回复列表
        List<DoctorReplyEntity> replies = consultationService.listReplies(id);

        // 填充医生姓名
        for (DoctorReplyEntity reply : replies) {
            if (reply.getDoctorId() != null) {
                UserEntity doctor = userService.findById(reply.getDoctorId());
                if (doctor != null) {
                    reply.setDoctorName(doctor.getUsername());
                }
            }
        }

        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("replies", replies);
        return ApiResponse.success(result);
    }

    /**
     * 查询我的问诊帖
     *
     * 请求：GET /api/consultation/posts/my
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * @param user 当前登录用户
     * @return 帖子列表
     */
    @GetMapping("/posts/my")
    public ApiResponse<List<ConsultationPostEntity>> myPosts(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<ConsultationPostEntity> posts = consultationService.listPostsByUserId(user.getId());
        return ApiResponse.success(posts);
    }

    /**
     * 医生回复帖子
     *
     * 请求：POST /api/consultation/posts/{id}/reply
     * 请求头：Authorization: Bearer {token}
     * 请求体：{"content": "根据您描述的症状，可能是偏头痛..."}
     * 响应：{"code": 200, "message": "回复成功", "data": {...}}
     *
     * @param user 当前登录用户（医生）
     * @param id 帖子ID
     * @param reply 回复内容
     * @return 创建成功的回复
     */
    @PostMapping("/posts/{id}/reply")
    public ApiResponse<DoctorReplyEntity> addReply(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id,
            @RequestBody DoctorReplyEntity reply) {
        reply.setPostId(id);
        reply.setDoctorId(user.getId());
        DoctorReplyEntity created = consultationService.addReply(reply);
        return ApiResponse.success("回复成功", created);
    }

    /**
     * 关闭问诊帖
     *
     * 请求：POST /api/consultation/posts/{id}/close
     * 请求头：Authorization: Bearer {token}
     * 响应：{"code": 200, "message": "帖子已关闭"}
     *
     * 权限：只有帖子作者才能关闭
     *
     * @param user 当前登录用户
     * @param id 帖子ID
     * @return 关闭结果
     */
    @PostMapping("/posts/{id}/close")
    public ApiResponse<String> closePost(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        // 查询帖子
        ConsultationPostEntity post = consultationService.findPostById(id);

        // 验证权限：只有帖子作者才能关闭
        if (post == null || !post.getUserId().equals(user.getId())) {
            return ApiResponse.error(403, "无权限操作");
        }

        consultationService.closePost(id);
        return ApiResponse.success("帖子已关闭", null);
    }
}
