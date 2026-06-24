package org.example.major_ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.major_ai.entity.ConsultationPostEntity;
import org.example.major_ai.entity.DoctorReplyEntity;
import org.example.major_ai.mapper.ConsultationPostMapper;
import org.example.major_ai.mapper.DoctorReplyMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationPostMapper consultationPostMapper;
    private final DoctorReplyMapper doctorReplyMapper;

    public ConsultationPostEntity createPost(ConsultationPostEntity post) {
        long now = System.currentTimeMillis();
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        post.setStatus("PENDING");
        consultationPostMapper.insert(post);
        return post;
    }

    public List<ConsultationPostEntity> listPosts(Long departmentId, String status) {
        LambdaQueryWrapper<ConsultationPostEntity> wrapper = new LambdaQueryWrapper<>();
        if (departmentId != null) {
            wrapper.eq(ConsultationPostEntity::getDepartmentId, departmentId);
        }
        if (status != null) {
            wrapper.eq(ConsultationPostEntity::getStatus, status);
        }
        wrapper.orderByDesc(ConsultationPostEntity::getCreatedAt);
        return consultationPostMapper.selectList(wrapper);
    }

    public ConsultationPostEntity findPostById(Long id) {
        return consultationPostMapper.selectById(id);
    }

    public List<ConsultationPostEntity> listPostsByUserId(Long userId) {
        return consultationPostMapper.selectList(
                new LambdaQueryWrapper<ConsultationPostEntity>()
                        .eq(ConsultationPostEntity::getUserId, userId)
                        .orderByDesc(ConsultationPostEntity::getCreatedAt)
        );
    }

    // 医生回复
    public DoctorReplyEntity addReply(DoctorReplyEntity reply) {
        reply.setCreatedAt(System.currentTimeMillis());
        doctorReplyMapper.insert(reply);

        // 更新帖子状态为已回复
        ConsultationPostEntity post = new ConsultationPostEntity();
        post.setId(reply.getPostId());
        post.setStatus("REPLIED");
        post.setUpdatedAt(System.currentTimeMillis());
        consultationPostMapper.updateById(post);

        return reply;
    }

    public List<DoctorReplyEntity> listReplies(Long postId) {
        return doctorReplyMapper.selectList(
                new LambdaQueryWrapper<DoctorReplyEntity>()
                        .eq(DoctorReplyEntity::getPostId, postId)
                        .orderByAsc(DoctorReplyEntity::getCreatedAt)
        );
    }

    public boolean closePost(Long postId) {
        ConsultationPostEntity post = new ConsultationPostEntity();
        post.setId(postId);
        post.setStatus("CLOSED");
        post.setUpdatedAt(System.currentTimeMillis());
        return consultationPostMapper.updateById(post) > 0;
    }
}
