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

@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;
    private final UserService userService;

    @PostMapping("/posts")
    public ApiResponse<ConsultationPostEntity> createPost(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody ConsultationPostEntity post) {
        post.setUserId(user.getId());
        ConsultationPostEntity created = consultationService.createPost(post);
        return ApiResponse.success("问诊帖发布成功", created);
    }

    @GetMapping("/posts")
    public ApiResponse<List<ConsultationPostEntity>> listPosts(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status) {
        List<ConsultationPostEntity> posts = consultationService.listPosts(departmentId, status);
        return ApiResponse.success(posts);
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<Map<String, Object>> getPostDetail(
            @PathVariable Long id) {
        ConsultationPostEntity post = consultationService.findPostById(id);
        if (post == null) {
            return ApiResponse.error(404, "帖子不存在");
        }
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

        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("replies", replies);
        return ApiResponse.success(result);
    }

    @GetMapping("/posts/my")
    public ApiResponse<List<ConsultationPostEntity>> myPosts(
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<ConsultationPostEntity> posts = consultationService.listPostsByUserId(user.getId());
        return ApiResponse.success(posts);
    }

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

    @PostMapping("/posts/{id}/close")
    public ApiResponse<String> closePost(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable Long id) {
        ConsultationPostEntity post = consultationService.findPostById(id);
        if (post == null || !post.getUserId().equals(user.getId())) {
            return ApiResponse.error(403, "无权限操作");
        }
        consultationService.closePost(id);
        return ApiResponse.success("帖子已关闭", null);
    }
}
