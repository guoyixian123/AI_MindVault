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
@RequestMapping("/api/admin/consultation")
@RequiredArgsConstructor
public class AdminConsultationController {

    private final ConsultationService consultationService;
    private final UserService userService;

    @GetMapping("/posts")
    public ApiResponse<List<ConsultationPostEntity>> listPosts(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(required = false) String status) {
        UserEntity doctor = userService.findById(user.getId());
        Long departmentId = doctor.getDepartmentId();

        List<ConsultationPostEntity> posts;
        if (departmentId != null) {
            // 医生只能看到自己科室的帖子
            posts = consultationService.listPosts(departmentId, status);
        } else {
            // 根管理员可以看到所有帖子
            posts = consultationService.listPosts(null, status);
        }
        return ApiResponse.success(posts);
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<Map<String, Object>> getPostDetail(@PathVariable Long id) {
        ConsultationPostEntity post = consultationService.findPostById(id);
        if (post == null) {
            return ApiResponse.error(404, "帖子不存在");
        }
        List<DoctorReplyEntity> replies = consultationService.listReplies(id);

        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("replies", replies);
        return ApiResponse.success(result);
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
}
