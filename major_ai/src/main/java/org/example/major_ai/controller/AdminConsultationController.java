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
 * 管理后台问诊控制器
 * 提供医生回复问诊帖的接口
 *
 * API前缀：/api/admin/consultation
 *
 * 权限：需要ROOT_ADMIN或DOCTOR角色
 *
 * 数据隔离：
 * - 医生只能看到自己科室的帖子
 * - ROOT_ADMIN可以看到所有帖子
 */
@RestController
@RequestMapping("/api/admin/consultation")
@RequiredArgsConstructor
public class AdminConsultationController {

    /** 问诊服务 */
    private final ConsultationService consultationService;

    /** 用户服务 */
    private final UserService userService;

    /**
     * 查询问诊帖列表
     *
     * 请求：GET /api/admin/consultation/posts?status=PENDING
     * 请求头：Authorization: Bearer {token}（需要ROOT_ADMIN或DOCTOR角色）
     * 响应：{"code": 200, "data": [{...}, {...}]}
     *
     * 数据隔离：
     * - 医生：只能看到自己科室的帖子
     * - ROOT_ADMIN：可以看到所有帖子
     *
     * @param user 当前登录用户
     * @param status 状态筛选（可选）
     * @return 帖子列表
     */
    @GetMapping("/posts")
    public ApiResponse<List<ConsultationPostEntity>> listPosts(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(required = false) String status) {
        // 查询当前用户信息
        UserEntity doctor = userService.findById(user.getId());
        Long departmentId = doctor.getDepartmentId();

        List<ConsultationPostEntity> posts;
        if (departmentId != null) {
            // 医生只能看到自己科室的帖子
            posts = consultationService.listPosts(departmentId, status);
        } else {
            // ROOT_ADMIN可以看到所有帖子
            posts = consultationService.listPosts(null, status);
        }
        fillUserNames(posts);
        return ApiResponse.success(posts);
    }

    /**
     * 查看帖子详情
     */
    @GetMapping("/posts/{id}")
    public ApiResponse<Map<String, Object>> getPostDetail(@PathVariable Long id) {
        ConsultationPostEntity post = consultationService.findPostById(id);
        if (post == null) {
            return ApiResponse.error(404, "帖子不存在");
        }

        fillUserName(post);

        List<DoctorReplyEntity> replies = consultationService.listReplies(id);
        for (DoctorReplyEntity reply : replies) {
            if (reply.getDoctorId() != null) {
                UserEntity doctor = userService.findById(reply.getDoctorId());
                if (doctor != null) {
                    reply.setDoctorName(doctor.getNickname() != null ? doctor.getNickname() : doctor.getUsername());
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("replies", replies);
        return ApiResponse.success(result);
    }

    private void fillUserNames(List<ConsultationPostEntity> posts) {
        for (ConsultationPostEntity post : posts) {
            if (post.getUserId() != null) {
                UserEntity u = userService.findById(post.getUserId());
                if (u != null) {
                    post.setUserName(u.getNickname() != null ? u.getNickname() : u.getUsername());
                }
            }
        }
    }

    private void fillUserName(ConsultationPostEntity post) {
        if (post.getUserId() != null) {
            UserEntity u = userService.findById(post.getUserId());
            if (u != null) {
                post.setUserName(u.getNickname() != null ? u.getNickname() : u.getUsername());
            }
        }
    }

    /**
     * 医生回复帖子
     *
     * 请求：POST /api/admin/consultation/posts/{id}/reply
     * 请求头：Authorization: Bearer {token}（需要ROOT_ADMIN或DOCTOR角色）
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
}
